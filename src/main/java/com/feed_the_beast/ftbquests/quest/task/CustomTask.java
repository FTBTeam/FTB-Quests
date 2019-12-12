package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.net.MessageSubmitTask;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
		void check(CustomTask.Data taskData, ServerPlayerEntity player);
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
	@OnlyIn(Dist.CLIENT)
	public void onButtonClicked(Button button, boolean canClick)
	{
		if (enableButton && canClick)
		{
			button.playClickSound();
			new MessageSubmitTask(id).sendToServer();
		}
	}

	@Override
	public int autoSubmitOnPlayerTick()
	{
		return check == null ? 0 : checkTimer;
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeVarInt(checkTimer);
		buffer.writeVarLong(maxProgress);
		buffer.writeBoolean(enableButton);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		checkTimer = buffer.readVarInt();
		maxProgress = buffer.readVarLong();
		enableButton = buffer.readBoolean();
	}

	@Override
	public TaskData createData(PlayerData data)
	{
		return new Data(this, data);
	}

	public static class Data extends TaskData<CustomTask>
	{
		private Data(CustomTask task, PlayerData data)
		{
			super(task, data);
		}

		@Override
		public void submitTask(ServerPlayerEntity player, ItemStack item)
		{
			if (task.check != null && !isComplete())
			{
				task.check.check(this, player);
			}
		}
	}
}