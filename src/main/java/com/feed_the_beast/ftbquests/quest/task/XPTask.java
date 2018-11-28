package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigLong;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.Collection;

/**
 * @author LatvianModder
 */
public class XPTask extends QuestTask implements ISingleLongValueTask
{
	public long value = 1L;

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
		return StringUtils.formatDouble(value, true);
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setLong("value", value);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		value = nbt.getLong("value");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarLong(value);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		value = data.readVarLong();
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
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addLong("value", () -> value, v -> value = v, 1000L, 1L, Long.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.xp_levels"));
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		ITextComponent text = new TextComponentString(getMaxProgressString());
		text.getStyle().setColor(TextFormatting.RED);
		return new TextComponentTranslation("ftbquests.reward.ftbquests.xp_levels.text", text);
	}

	@Override
	public QuestTaskData createData(ITeamData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<XPTask>
	{
		private Data(XPTask task, ITeamData data)
		{
			super(task, data);
		}

		@Override
		public boolean submitTask(EntityPlayerMP player, Collection<ItemStack> itemsToCheck, boolean simulate)
		{
			int add = (int) Math.min(player.experienceLevel, Math.min(task.value - progress, Integer.MAX_VALUE));

			if (add > 0)
			{
				if (!simulate)
				{
					player.addExperienceLevel(-add);
					progress += add;
					sync();
				}

				return true;
			}

			return false;
		}
	}
}