package dev.ftb.mods.ftbquests;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.ChangeProgress;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class FTBQuestsNetCommon {
	public void changeProgress(UUID player, long id, ChangeProgress type, boolean notifications) {
	}

	public void claimReward(UUID player, long id) {
	}

	public void createObject(long id, long parent, QuestObjectType type, CompoundTag nbt, CompoundTag extra) {
	}

	public void createPlayerData(UUID uuid, String name) {
	}

	public void deleteObject(long id) {
	}

	public void displayCompletionToast(long id) {
	}

	public void displayItemRewardToast(ItemStack stack, int count) {
	}

	public void displayRewardToast(long id, Component text, Icon icon) {
	}

	public void editObject(long id, CompoundTag nbt) {
	}

	public void moveChapter(long id, boolean up) {
	}

	public void moveQuest(long id, long chapter, double x, double y) {
	}

	public void syncEditingMode(boolean editingMode) {
	}

	public void togglePinned(long id) {
	}

	public void updatePlayerData(UUID uuid, String name) {
	}

	public void updateTaskProgress(UUID player, long task, long progress) {
	}

	public void changeChapterGroup(long id, long group) {
	}

	public void moveChapterGroup(long id, boolean up) {
	}
}