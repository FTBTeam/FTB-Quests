package dev.ftb.mods.ftbquests.quest.task;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.util.KnownServerRegistries;
import dev.ftb.mods.ftblibrary.util.NameMap;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.List;

public class DimensionTask extends AbstractBooleanTask {
	private ResourceKey<Level> dimension;

	public DimensionTask(long id, Quest quest) {
		super(id, quest);

		dimension = Level.NETHER;
	}

	public DimensionTask withDimension(ResourceKey<Level> dimension) {
		this.dimension = dimension;
		return this;
	}

	@Override
	public TaskType getType() {
		return TaskTypes.DIMENSION;
	}

	@Override
	public void writeData(Json5Object json, HolderLookup.Provider provider) {
		super.writeData(json, provider);
		Json5Util.store(json, "dimension", Identifier.CODEC, dimension.identifier());
	}

	@Override
	public void readData(Json5Object json, HolderLookup.Provider provider) {
		super.readData(json, provider);
		Identifier dimId = Json5Util.fetch(json, "dimension", Identifier.CODEC).orElseThrow();
		dimension = ResourceKey.create(Registries.DIMENSION, dimId);
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeIdentifier(dimension.identifier());
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		dimension = ResourceKey.create(Registries.DIMENSION, buffer.readIdentifier());
	}

	@Override
	public void fillConfigGroup(EditableConfigGroup config) {
		super.fillConfigGroup(config);

		if (KnownServerRegistries.client != null && !KnownServerRegistries.client.dimension().isEmpty()) {
			List<Identifier> dimensions = KnownServerRegistries.client.dimension();
			config.addEnum("dim", dimension.identifier(), v -> dimension = ResourceKey.create(Registries.DIMENSION, v),
					NameMap.of(dimensions.getFirst(), dimensions.toArray(new Identifier[0])).create()
			);
		} else {
			config.addString("dim", dimension.identifier().toString(), v -> Identifier.read(v).ifSuccess(id -> dimension = ResourceKey.create(Registries.DIMENSION, id)), "minecraft:the_nether");
		}
	}

	@Override
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.task.ftbquests.dimension").append(": ").append(Component.literal(dimension.identifier().toString()).withStyle(ChatFormatting.DARK_GREEN));
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return 100;
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		return !player.isSpectator() && player.level().dimension() == dimension;
	}
}
