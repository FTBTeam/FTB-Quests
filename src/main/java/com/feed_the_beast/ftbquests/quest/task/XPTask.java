package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigLong;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

import java.util.Collection;

/**
 * @author LatvianModder
 */
public class XPTask extends QuestTask implements ISingleLongValueTask
{
	public long value = 1L;
	public boolean points = false;

	public XPTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public QuestTaskType getType()
	{
		return FTBQuestsTasks.XP;
	}

	@Override
	public long getMaxProgress()
	{
		return value;
	}

	@Override
	public String getMaxProgressString()
	{
		return Long.toUnsignedString(points && value <= Integer.MAX_VALUE ? getLevelForExperience((int) value) : value);
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setLong("value", value);
		nbt.setBoolean("points", points);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		value = nbt.getLong("value");
		points = nbt.getBoolean("points");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarLong(value);
		data.writeBoolean(points);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		value = data.readVarLong();
		points = data.readBoolean();
	}

	@Override
	public ConfigLong getDefaultValue()
	{
		return new ConfigLong(value, 1L, Long.MAX_VALUE);
	}

	@Override
	public void setValue(long v)
	{
		value = v;
	}

	@Override
	public void getConfig(EntityPlayer player, ConfigGroup config)
	{
		super.getConfig(player, config);
		config.addLong("value", () -> value, v -> value = v, 1L, 1L, Long.MAX_VALUE);
		config.addBool("points", () -> points, v -> points = v, false);
	}

	@Override
	public String getAltTitle()
	{
		return I18n.format("ftbquests.reward.ftbquests.xp_levels") + ": " + TextFormatting.RED + getMaxProgressString();
	}

	@Override
	public boolean consumesResources()
	{
		return true;
	}

	@Override
	public QuestTaskData createData(QuestData data)
	{
		return new Data(this, data);
	}

	public static int getPlayerXP(EntityPlayer player)
	{
		return (int) (getExperienceForLevel(player.experienceLevel) + (player.experience * player.xpBarCap()));
	}

	public static void addPlayerXP(EntityPlayer player, int amount)
	{
		int experience = getPlayerXP(player) + amount;
		player.experienceTotal = experience;
		player.experienceLevel = getLevelForExperience(experience);
		int expForLevel = getExperienceForLevel(player.experienceLevel);
		player.experience = (float) (experience - expForLevel) / (float) player.xpBarCap();
	}

	public static int xpBarCap(int level)
	{
		if (level >= 30)
		{
			return 112 + (level - 30) * 9;
		}

		if (level >= 15)
		{
			return 37 + (level - 15) * 5;
		}

		return 7 + level * 2;
	}

	private static int sum(int n, int a0, int d)
	{
		return n * (2 * a0 + (n - 1) * d) / 2;
	}

	public static int getExperienceForLevel(int level)
	{
		if (level == 0)
		{
			return 0;
		}

		if (level <= 15)
		{
			return sum(level, 7, 2);
		}

		if (level <= 30)
		{
			return 315 + sum(level - 15, 37, 5);
		}

		return 1395 + sum(level - 30, 112, 9);
	}

	public static int getLevelForExperience(int targetXp)
	{
		int level = 0;

		while (true)
		{
			final int xpToNextLevel = xpBarCap(level);

			if (targetXp < xpToNextLevel)
			{
				return level;
			}

			level++;
			targetXp -= xpToNextLevel;
		}
	}

	public static class Data extends SimpleQuestTaskData<XPTask>
	{
		private Data(XPTask task, QuestData data)
		{
			super(task, data);
		}

		@Override
		public String getProgressString()
		{
			return Long.toUnsignedString(task.points && task.value <= Integer.MAX_VALUE ? getLevelForExperience((int) progress) : progress);
		}

		@Override
		public boolean submitTask(EntityPlayerMP player, Collection<ItemStack> itemsToCheck, boolean simulate)
		{
			int add = (int) Math.min(task.points ? getPlayerXP(player) : player.experienceLevel, Math.min(task.value - progress, Integer.MAX_VALUE));

			if (add > 0)
			{
				if (!simulate)
				{
					if (task.points)
					{
						addPlayerXP(player, -add);
						player.addExperienceLevel(0);
					}
					else
					{
						player.addExperienceLevel(-add);
					}

					progress += add;
					sync();
				}

				return true;
			}

			return false;
		}
	}
}