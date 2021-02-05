package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.ChapterGroup;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.Task;
import me.shedaniel.architectury.hooks.PlayerHooks;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class FTBQuestsInventoryListener implements ContainerListener
{
	public final ServerPlayer player;

	public FTBQuestsInventoryListener(ServerPlayer p)
	{
		player = p;
	}

	public static void detect(ServerPlayer player, ItemStack item, long sourceTask)
	{
		if (ServerQuestFile.INSTANCE == null || PlayerHooks.isFake(player))
		{
			return;
		}

		PlayerData data = ServerQuestFile.INSTANCE.getNullablePlayerData(player.getUUID());

		if (data == null)
		{
			return;
		}

		for (ChapterGroup group : ServerQuestFile.INSTANCE.chapterGroups)
		{
			for (Chapter chapter : group.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					if (hasSubmitTasks(quest) && data.canStartTasks(quest))
					{
						for (Task task : quest.tasks)
						{
							if (task.id != sourceTask && task.submitItemsOnInventoryChange())
							{
								data.getTaskData(task).submitTask(player, item);
							}
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
	public void refreshContainer(AbstractContainerMenu container, NonNullList<ItemStack> itemsList)
	{
		detect(player, ItemStack.EMPTY, 0);
	}

	@Override
	public void slotChanged(AbstractContainerMenu container, int index, ItemStack stack)
	{
		if (!stack.isEmpty() && container.getSlot(index).container == player.inventory)
		{
			detect(player, ItemStack.EMPTY, 0);
		}
	}

	@Override
	public void setContainerData(AbstractContainerMenu container, int id, int value)
	{
	}
}