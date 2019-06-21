package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;

import java.util.Collection;

/**
 * @author LatvianModder
 */
public class StatTask extends QuestTask
{
	public StatBase stat = StatList.MOB_KILLS;
	public int value = 1;

	public static StatBase get(String id)
	{
		for (StatBase base : StatList.BASIC_STATS)
		{
			if (base.statId.equals(id))
			{
				return base;
			}
		}

		return StatList.MOB_KILLS;
	}

	public StatTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public QuestTaskType getType()
	{
		return FTBQuestsTasks.STAT;
	}

	@Override
	public long getMaxProgress()
	{
		return value;
	}

	@Override
	public String getMaxProgressString()
	{
		return Integer.toString(value);
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setString("stat", stat.statId);
		nbt.setInteger("value", value);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		stat = get(nbt.getString("stat"));
		value = nbt.getInteger("value");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeString(stat.statId);
		data.writeVarInt(value);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		stat = get(data.readString());
		value = data.readVarInt();
	}

	@Override
	public void getConfig(EntityPlayer player, ConfigGroup config)
	{
		super.getConfig(player, config);
		config.addEnum("stat", () -> stat, v -> stat = v, NameMap.create(StatList.MOB_KILLS, NameMap.ObjectProperties.withName((sender, s) -> s.getStatName()), StatList.BASIC_STATS.toArray(new StatBase[0])));
		config.addInt("value", () -> value, v -> value = v, 1, 1, Integer.MAX_VALUE);
	}

	@Override
	public String getAltTitle()
	{
		return stat.getStatName().getUnformattedText();
	}

	@Override
	public boolean consumesResources()
	{
		return true;
	}

	@Override
	public boolean autoSubmitOnPlayerTick()
	{
		return true;
	}

	@Override
	public QuestTaskData createData(ITeamData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<StatTask>
	{
		private Data(StatTask task, ITeamData data)
		{
			super(task, data);
		}

		@Override
		public String getProgressString()
		{
			return Integer.toString((int) progress);
		}

		@Override
		public boolean submitTask(EntityPlayerMP player, Collection<ItemStack> itemsToCheck, boolean simulate)
		{
			if (progress >= task.value)
			{
				return false;
			}

			int set = Math.min(task.value, player.getStatFile().readStat(task.stat));

			if (set > progress)
			{
				if (!simulate)
				{
					progress = set;
					sync();
				}

				return true;
			}

			return false;
		}
	}
}