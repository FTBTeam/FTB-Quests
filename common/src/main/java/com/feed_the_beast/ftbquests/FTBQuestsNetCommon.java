package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class FTBQuestsNetCommon
{
	public void changeProgress(UUID player, int id, ChangeProgress type, boolean notifications)
	{
	}

	public void claimReward(UUID player, int id)
	{
	}

	public void createObject(int id, int parent, QuestObjectType type, CompoundTag nbt, CompoundTag extra)
	{
	}

	public void createPlayerData(UUID uuid, String name)
	{
	}

	public void deleteObject(int id)
	{
	}

	public void displayCompletionToast(int id)
	{
	}

	public void displayItemRewardToast(ItemStack stack, int count)
	{
	}

	public void displayRewardToast(int id, Component text, Icon icon)
	{
	}

	public void editObject(int id, CompoundTag nbt)
	{
	}

	public void moveChapter(int id, boolean up)
	{
	}

	public void moveQuest(int id, int chapter, double x, double y)
	{
	}

	public void syncEditingMode(boolean editingMode)
	{
	}

	public void togglePinned(int id)
	{
	}

	public void updatePlayerData(UUID uuid, String name)
	{
	}

	public void updateTaskProgress(UUID player, int task, long progress)
	{
	}
}