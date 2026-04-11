package dev.ftb.mods.ftbquests.quest.task;

import com.mojang.datafixers.util.Either;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftblibrary.util.NameMap;
import dev.ftb.mods.ftbquests.net.SyncStructuresRequestMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

public class StructureTask extends AbstractBooleanTask {
	private static final Identifier DEFAULT_STRUCTURE = Identifier.withDefaultNamespace("mineshaft");

	private static final List<String> KNOWN_STRUCTURES = new ArrayList<>();

	private Either<ResourceKey<Structure>, TagKey<Structure>> structure;

	public StructureTask(long id, Quest quest) {
		super(id, quest);
		structure = Either.left(ResourceKey.create(Registries.STRUCTURE, DEFAULT_STRUCTURE));
	}

	public static void syncKnownStructureList(List<String> data) {
		// receive structure data from server (structure registry doesn't exist on client)
		KNOWN_STRUCTURES.clear();
		KNOWN_STRUCTURES.addAll(data);
	}

	@Override
	public TaskType getType() {
		return TaskTypes.STRUCTURE;
	}

	@Override
	public void writeData(@UnknownNullability Json5Object json, HolderLookup.Provider provider) {
		super.writeData(json, provider);
		json.addProperty("structure", getStructure());
	}

	@Override
	public void readData(@UnknownNullability Json5Object json, HolderLookup.Provider provider) {
		super.readData(json, provider);
		setStructure(Json5Util.getString(json, "structure").orElseThrow());
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(getStructure());
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		setStructure(buffer.readUtf(1024));
	}

	@Override
	public void fillConfigGroup(EditableConfigGroup config) {
		super.fillConfigGroup(config);
		if (KNOWN_STRUCTURES.isEmpty()) {
			// should not normally be the case, but as a defensive fallback...
			config.addString("structure", getStructure(), this::setStructure, "minecraft:mineshaft");
		} else {
			config.addEnum("structure", getStructure(), this::setStructure, NameMap.of(DEFAULT_STRUCTURE.toString(), KNOWN_STRUCTURES).create());
		}
	}

	@Override
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.task.ftbquests.structure")
				.append(": ").append(Component.literal(getStructure()).withStyle(ChatFormatting.DARK_GREEN));
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return 20;
	}

	@Override
	public boolean checkOnLogin() {
		// checking on login can cause server lag: https://github.com/FTBTeam/FTB-Mods-Issues/issues/799
		return false;
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		if (player.isSpectator()) return false;

		ServerLevel level = player.level();
		StructureManager mgr = level.structureManager();
		return structure.map(
				key -> {
					Structure structure = mgr.registryAccess().lookupOrThrow(Registries.STRUCTURE).getValue(key);
					return structure != null && mgr.getStructureWithPieceAt(player.blockPosition(), structure).isValid();
				},
				tag -> mgr.getStructureWithPieceAt(player.blockPosition(), tag).isValid()
		);
	}

	private void setStructure(String resLoc) {
		structure = resLoc.startsWith("#") ?
				Either.right(TagKey.create(Registries.STRUCTURE, safeResourceLocation(resLoc.substring(1), DEFAULT_STRUCTURE))) :
				Either.left(ResourceKey.create(Registries.STRUCTURE, safeResourceLocation(resLoc, DEFAULT_STRUCTURE)));
	}

	private String getStructure() {
		return structure.map(
				key -> key.identifier().toString(),
				tag -> "#" + tag.location()
		);
	}

	public static void maybeRequestStructureSync() {
		if (KNOWN_STRUCTURES.isEmpty()) {
			Play2ServerNetworking.send(SyncStructuresRequestMessage.INSTANCE);
		}
	}
}
