package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftbguilibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * @author LatvianModder
 */
public class BiomeTask extends BooleanTask {
	public ResourceKey<Biome> biome;

	public BiomeTask(Quest quest) {
		super(quest);
		biome = Biomes.PLAINS;
	}

	@Override
	public TaskType getType() {
		return TaskTypes.BIOME;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("biome", biome.location().toString());
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		biome = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(nbt.getString("biome")));
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeResourceLocation(biome.location());
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		biome = ResourceKey.create(Registry.BIOME_REGISTRY, buffer.readResourceLocation());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addString("biome", biome.location().toString(), v -> biome = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(v)), "minecraft:plains");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return new TranslatableComponent("ftbquests.task.ftbquests.biome").append(": ").append(new TextComponent(biome.location().toString()).withStyle(ChatFormatting.DARK_GREEN));
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return 20;
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		return !player.isSpectator() && player.level.getBiomeName(player.blockPosition()).orElse(null) == biome;
	}
}