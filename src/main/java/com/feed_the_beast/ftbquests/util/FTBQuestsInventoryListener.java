package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.Task;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * @author LatvianModder
 */
public class FTBQuestsInventoryListener implements IContainerListener
{
	public final ServerPlayerEntity player;

	public FTBQuestsInventoryListener(ServerPlayerEntity p)
	{
		player = p;
	}

	public static void detect(ServerPlayerEntity player, ItemStack item)
	{
		if (ServerQuestFile.INSTANCE == null)
		{
			return;
		}

		PlayerData data = ServerQuestFile.INSTANCE.getData(player);

		if (data == null)
		{
			return;
		}

		boolean redo = false;

		for (Chapter chapter : ServerQuestFile.INSTANCE.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				if (hasSubmitTasks(quest) && data.canStartTasks(quest))
				{
					for (Task task : quest.tasks)
					{
						if (task.submitItemsOnInventoryChange())
						{
							data.getTaskData(task).submitTask(player, item);
						}
					}
				}
			}
		}
	}

	private static boolean hasSubmitTasks(Quest quest)
	{
		for (Task task : quest.tasks)
		{
			if (task.submitItemsOnInventoryChange())
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public void sendAllContents(Container container, NonNullList<ItemStack> itemsList)
	{
		detect(player, ItemStack.EMPTY);
	}

	@Override
	public void sendSlotContents(Container container, int index, ItemStack stack)
	{
		if (!stack.isEmpty() && container.getSlot(index).inventory == player.inventory)
		{
			detect(player, ItemStack.EMPTY);
		}
	}

	@Override
	public void sendWindowProperty(Container container, int id, int value)
	{
	}
}