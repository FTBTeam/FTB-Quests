package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.util.RayMatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;

/**
 * @author LatvianModder
 */
public class InteractionTask extends QuestTask
{
	public final RayMatcher matcher;

	public InteractionTask(Quest quest)
	{
		super(quest);
		matcher = new RayMatcher();
	}

	@Override
	public QuestTaskType getType()
	{
		return FTBQuestsTasks.INTERACTION;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		matcher.writeData(nbt);
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		matcher.readData(nbt);
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		matcher.writeNetData(data);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		matcher.readNetData(data);
	}

	@Override
	public void getConfig(EntityPlayer player, ConfigGroup config)
	{
		super.getConfig(player, config);
		config.addEnum("type", () -> matcher.type, v -> matcher.type = v, RayMatcher.Type.NAME_MAP);
		config.addString("match", () -> matcher.match, v -> matcher.match = v, "");
		config.addString("properties", matcher::getPropertyString, matcher::setPropertyString, "");
	}

	@Override
	public void onButtonClicked(boolean canClick)
	{
	}

	@Override
	public QuestTaskData createData(QuestData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<InteractionTask>
	{
		private Data(InteractionTask task, QuestData data)
		{
			super(task, data);
		}

		@Override
		public boolean submitTask(EntityPlayerMP player, Collection<ItemStack> itemsToCheck, boolean simulate)
		{
			if (progress < 1L && task.matcher.matches(RayMatcher.Data.get(player)))
			{
				if (!simulate)
				{
					progress = 1L;
					sync();
				}

				return true;
			}

			return false;
		}
	}
}