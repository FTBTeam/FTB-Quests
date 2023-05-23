package dev.ftb.mods.ftbquests.quest.task;

import com.mojang.datafixers.util.Either;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.net.SyncStructuresRequestMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MaxNeedsSnacks
 */
public class StructureTask extends BooleanTask {
	private static final ResourceLocation DEFAULT_STRUCTURE = new ResourceLocation("minecraft:mineshaft");

	private static final List<String> KNOWN_STRUCTURES = new ArrayList<>();

	private Either<ResourceKey<Structure>, TagKey<Structure>> structure;

	public StructureTask(Quest quest) {
		super(quest);
		structure = Either.left(ResourceKey.create(Registry.STRUCTURE_REGISTRY, DEFAULT_STRUCTURE));
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
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("structure", getStructure());
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		setStructure(nbt.getString("structure"));
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(getStructure());
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		setStructure(buffer.readUtf(1024));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		if (KNOWN_STRUCTURES.isEmpty()) {
			// should not normally be the case, but as a defensive fallback...
			config.addString("structure", getStructure(), this::setStructure, "minecraft:mineshaft");
		} else {
			config.addEnum("structure", getStructure(), this::setStructure, NameMap.of(DEFAULT_STRUCTURE.toString(), KNOWN_STRUCTURES).create());
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
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

		ServerLevel level = (ServerLevel) player.level;
		return structure.map(
				key -> level.structureManager().getStructureWithPieceAt(player.blockPosition(), key).isValid(),
				tag -> level.structureManager().getStructureWithPieceAt(player.blockPosition(), tag).isValid()
		);
	}

	private void setStructure(String resLoc) {
		structure = resLoc.startsWith("#") ?
				Either.right(TagKey.create(Registry.STRUCTURE_REGISTRY, safeResourceLocation(resLoc.substring(1), DEFAULT_STRUCTURE))) :
				Either.left(ResourceKey.create(Registry.STRUCTURE_REGISTRY, safeResourceLocation(resLoc, DEFAULT_STRUCTURE)));
	}

	private String getStructure() {
		return structure.map(
				key -> key.location().toString(),
				tag -> "#" + tag.location()
		);
	}

	public static void maybeRequestStructureSync() {
		if (KNOWN_STRUCTURES.isEmpty()) {
			new SyncStructuresRequestMessage().sendToServer();
		}
	}
}
