package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
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
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * @author MaxNeedsSnacks
 */
public class StructureTask extends BooleanTask {
	// TODO: validate
	public ResourceKey<Structure> structure;

	public StructureTask(Quest quest) {
		super(quest);
		structure = ResourceKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation("minecraft:mineshaft"));
	}

	@Override
	public TaskType getType() {
		return TaskTypes.STRUCTURE;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("structure", structure.location().toString());
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		structure = ResourceKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(nbt.getString("structure")));
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeResourceLocation(structure.location());
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		structure = ResourceKey.create(Registry.STRUCTURE_REGISTRY, buffer.readResourceLocation());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addString("structure", structure.location().toString(), v -> structure = ResourceKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(v)), "minecraft:mineshaft");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.task.ftbquests.structure").append(": ").append(Component.literal(structure.location().toString()).withStyle(ChatFormatting.DARK_GREEN));
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return 20;
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		return ((ServerLevel) player.level).structureManager().getStructureWithPieceAt(player.blockPosition(), structure).isValid();
	}
}
