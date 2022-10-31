package dev.ftb.mods.ftbquests;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.net.TeamDataUpdate;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

public class FTBQuestsNetCommon {
	public void syncTeamData(boolean self, TeamData data) {
	}

	public void claimReward(UUID teamId, UUID player, long reward) {
	}

	public void createObject(long id, long parent, QuestObjectType type, CompoundTag nbt, CompoundTag extra) {
	}

	public void createOtherTeamData(TeamDataUpdate dataUpdate) {
	}

	public void teamDataChanged(TeamDataUpdate oldDataUpdate, TeamDataUpdate newDataUpdate) {
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

	public void syncEditingMode(UUID teamId, boolean editingMode) {
	}

	public void togglePinned(long id, boolean pinned) {
	}

	public void updateTeamData(UUID teamId, String name) {
	}

	public void updateTaskProgress(UUID teamId, long task, long progress) {
	}

	public void changeChapterGroup(long id, long group) {
	}

	public void moveChapterGroup(long id, boolean up) {
	}

	public void objectStarted(UUID teamId, long id, @Nullable Date time) {
	}

	public void objectCompleted(UUID teamId, long id, @Nullable Date time) {
	}

	public void syncLock(UUID id, boolean lock) {
	}

	public void resetReward(UUID teamId, UUID player, long rewardId) {
	}

	public void toggleChapterPinned(boolean pinned) {
	}
}