package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author LatvianModder
 */
public class XPTask extends Task implements ISingleLongValueTask
{
	public long value = 1L;
	public boolean points = false;

	public XPTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public TaskType getType()
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
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putLong("value", value);
		nbt.putBoolean("points", points);
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		value = nbt.getLong("value");
		points = nbt.getBoolean("points");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeVarLong(value);
		buffer.writeBoolean(points);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		value = buffer.readVarLong();
		points = buffer.readBoolean();
	}

	@Override
	public void setValue(long v)
	{
		value = v;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addLong("value", value, v -> value = v, 1L, 1L, Long.MAX_VALUE);
		config.addBool("points", points, v -> points = v, false);
	}

	@Override
	public IFormattableTextComponent getAltTitle()
	{
		return new TranslationTextComponent("ftbquests.reward.ftbquests.xp_levels").appendString(": ").append(new StringTextComponent(getMaxProgressString()).mergeStyle(TextFormatting.RED));
	}

	@Override
	public boolean consumesResources()
	{
		return true;
	}

	@Override
	public TaskData createData(PlayerData data)
	{
		return new Data(this, data);
	}

	public static int getPlayerXP(PlayerEntity player)
	{
		return (int) (getExperienceForLevel(player.experienceLevel) + (player.experience * player.xpBarCap()));
	}

	public static void addPlayerXP(PlayerEntity player, int amount)
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

	public static class Data extends TaskData<XPTask>
	{
		private Data(XPTask task, PlayerData data)
		{
			super(task, data);
		}

		@Override
		public String getProgressString()
		{
			return Long.toUnsignedString(task.points && task.value <= Integer.MAX_VALUE ? getLevelForExperience((int) progress) : progress);
		}

		@Override
		public void submitTask(ServerPlayerEntity player, ItemStack item)
		{
			int add = (int) Math.min(task.points ? getPlayerXP(player) : player.experienceLevel, Math.min(task.value - progress, Integer.MAX_VALUE));

			if (add <= 0)
			{
				return;
			}

			if (task.points)
			{
				addPlayerXP(player, -add);
				player.addExperienceLevel(0);
			}
			else
			{
				player.addExperienceLevel(-add);
			}

			addProgress(add);
		}
	}
}