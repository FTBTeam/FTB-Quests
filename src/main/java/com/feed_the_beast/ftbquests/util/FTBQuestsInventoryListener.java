package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.Task;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.Collection;
import java.util.Collections;

/**
 * @author LatvianModder
 */
public class FTBQuestsInventoryListener implements IContainerListener
{
	private static int detectCounter = 0;

	public final EntityPlayerMP player;

	public FTBQuestsInventoryListener(EntityPlayerMP p)
	{
		player = p;
	}

	public static void detect(EntityPlayerMP player, Collection<ItemStack> itemsToCheck)
	{
		detectCounter = 0;
		detect0(player, itemsToCheck);
	}

	private static void detect0(EntityPlayerMP player, Collection<ItemStack> itemsToCheck)
	{
		if (++detectCounter >= 10)
		{
			return;
		}

		//FTBQuests.LOGGER.info("Running auto-completion with list: " + itemsToCheck);

		QuestData data = ServerQuestFile.INSTANCE.getData(player);

		if (data == null)
		{
			return;
		}

		boolean redo = false;

		for (Chapter chapter : ServerQuestFile.INSTANCE.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				if (hasSubmitTasks(quest) && quest.canStartTasks(data))
				{
					for (Task task : quest.tasks)
					{
						if (task.submitItemsOnInventoryChange() && data.getTaskData(task).submitTask(player, itemsToCheck, false))
						{
							redo = true;
						}
					}
				}
			}
		}

		if (redo)
		{
			detect(player, Collections.emptyList());
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
		detect(player, Collections.emptyList());
	}

	@Override
	public void sendSlotContents(Container container, int index, ItemStack stack)
	{
		if (!stack.isEmpty() && container.getSlot(index).inventory == player.inventory)
		{
			detect(player, Collections.singleton(stack));
		}
	}

	@Override
	public void sendWindowProperty(Container container, int id, int value)
	{
	}

	@Override
	public void sendAllWindowProperties(Container container, IInventory inventory)
	{
	}
}