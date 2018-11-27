package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
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
	public final EntityPlayerMP player;

	public FTBQuestsInventoryListener(EntityPlayerMP p)
	{
		player = p;
	}

	public void detect(Collection<ItemStack> itemsToCheck)
	{
		ITeamData data = null;
		boolean redo = false;

		for (QuestChapter chapter : ServerQuestFile.INSTANCE.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				for (QuestTask task : quest.tasks)
				{
					if (task.submitItemsOnInventoryChange())
					{
						if (data == null)
						{
							data = ServerQuestFile.INSTANCE.getData(FTBLibAPI.getTeamID(player.getUniqueID()));

							if (data == null)
							{
								return;
							}
						}

						if (data.getQuestTaskData(task).submitTask(player, itemsToCheck, false))
						{
							redo = true;
						}
					}
				}
			}
		}

		if (redo)
		{
			detect(Collections.emptyList());
		}
	}

	@Override
	public void sendAllContents(Container container, NonNullList<ItemStack> itemsList)
	{
		detect(Collections.emptyList());
	}

	@Override
	public void sendSlotContents(Container container, int index, ItemStack stack)
	{
		if (container.getSlot(index).inventory == player.inventory)
		{
			detect(Collections.singleton(stack));
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