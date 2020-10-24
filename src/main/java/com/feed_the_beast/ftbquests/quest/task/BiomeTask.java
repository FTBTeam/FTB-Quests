package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author LatvianModder
 */
public class BiomeTask extends Task
{
	public RegistryKey<Biome> biome;

	public BiomeTask(Quest quest)
	{
		super(quest);
		biome = Biomes.PLAINS;
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.BIOME;
	}

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putString("biome", biome.getLocation().toString());
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		biome = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, new ResourceLocation(nbt.getString("biome")));
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeResourceLocation(biome.getLocation());
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		biome = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, buffer.readResourceLocation());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("biome", biome.getLocation().toString(), v -> biome = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, new ResourceLocation(v)), "minecraft:plains");
	}

	@Override
	public IFormattableTextComponent getAltTitle()
	{
		return new TranslationTextComponent("ftbquests.task.ftbquests.biome").appendString(": ").append(new StringTextComponent(biome.getLocation().toString()).mergeStyle(TextFormatting.DARK_GREEN));
	}

	@Override
	public int autoSubmitOnPlayerTick()
	{
		return 20;
	}

	@Override
	public TaskData createData(PlayerData data)
	{
		return new Data(this, data);
	}

	public static class Data extends BooleanTaskData<BiomeTask>
	{
		private Data(BiomeTask task, PlayerData data)
		{
			super(task, data);
		}

		@Override
		public boolean canSubmit(ServerPlayerEntity player)
		{
			return !player.isSpectator() && player.world.func_242406_i(player.getPosition()).orElse(null) == task.biome;
		}
	}
}