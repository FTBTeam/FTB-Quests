package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.NameMap;
import com.google.common.collect.Streams;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.stream.Collectors;

/**
 * @author LatvianModder
 */
public class DimensionTask extends Task
{
	public String dimension;

	public DimensionTask(Quest quest)
	{
		super(quest);
		dimension = "minecraft:the_nether";
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.DIMENSION;
	}

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putString("dimension", dimension);
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		dimension = nbt.getString("dimension");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeString(dimension, Short.MAX_VALUE);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		dimension = buffer.readString(Short.MAX_VALUE);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addEnum("dim", dimension, v -> dimension = v, NameMap.of("minecraft:the_nether", Streams.stream(DimensionType.getAll()).map(d -> String.valueOf(d.getRegistryName())).collect(Collectors.toList())).create());
	}

	@Override
	public String getAltTitle()
	{
		return I18n.format("ftbquests.task.ftbquests.dimension") + ": " + TextFormatting.DARK_GREEN + dimension;
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

	public static class Data extends BooleanTaskData<DimensionTask>
	{
		private Data(DimensionTask task, PlayerData data)
		{
			super(task, data);
		}

		@Override
		public boolean canSubmit(ServerPlayerEntity player)
		{
			return task.dimension.equals(String.valueOf(player.dimension.getRegistryName())) && !player.isSpectator();
		}
	}
}