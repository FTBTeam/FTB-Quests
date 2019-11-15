package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.net.MessageSubmitTask;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class CustomTask extends Task
{
	public static final Predicate<QuestObjectBase> PREDICATE = object -> object instanceof CustomTask;

	@FunctionalInterface
	public interface Check
	{
		void check(CustomTask.Data taskData, EntityPlayerMP player);
	}

	public Check check;
	public int checkTimer;
	public long maxProgress;
	public boolean enableButton;

	public CustomTask(Quest quest)
	{
		super(quest);
		check = null;
		checkTimer = 1;
		maxProgress = 1L;
		enableButton = false;
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.CUSTOM;
	}

	@Override
	public long getMaxProgress()
	{
		return maxProgress;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onButtonClicked(boolean canClick)
	{
		if (enableButton && canClick)
		{
			GuiHelper.playClickSound();
			new MessageSubmitTask(id).sendToServer();
		}
	}

	@Override
	public int autoSubmitOnPlayerTick()
	{
		return check == null ? 0 : checkTimer;
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarInt(checkTimer);
		data.writeVarLong(maxProgress);
		data.writeBoolean(enableButton);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		checkTimer = data.readVarInt();
		maxProgress = data.readVarLong();
		enableButton = data.readBoolean();
	}

	@Override
	public TaskData createData(QuestData data)
	{
		return new Data(this, data);
	}

	public static class Data extends TaskData<CustomTask>
	{
		private Data(CustomTask task, QuestData data)
		{
			super(task, data);
		}

		@Override
		public void submitTask(EntityPlayerMP player, ItemStack item)
		{
			if (task.check != null && !isComplete())
			{
				task.check.check(this, player);
			}
		}
	}
}