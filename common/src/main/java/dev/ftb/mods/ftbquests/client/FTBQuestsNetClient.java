package dev.ftb.mods.ftbquests.client;

import dev.architectury.hooks.item.ItemStackHooks;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.gui.*;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.net.TeamDataUpdate;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

public class FTBQuestsNetClient {
	public static void syncTeamData(TeamData data) {
		ClientQuestFile.INSTANCE.addData(data, true);
		ClientQuestFile.INSTANCE.selfTeamData = data;
	}

	public static void claimReward(UUID teamId, UUID player, long rewardId) {
		Reward reward = ClientQuestFile.INSTANCE.getReward(rewardId);

		if (reward == null) {
			return;
		}

		TeamData data = ClientQuestFile.INSTANCE.getOrCreateTeamData(teamId);
		data.claimReward(player, reward, System.currentTimeMillis());

		if (data == ClientQuestFile.INSTANCE.selfTeamData) {
			QuestScreen treeGui = ClientUtils.getCurrentGuiAs(QuestScreen.class);
			if (treeGui != null) {
				treeGui.refreshViewQuestPanel();
				treeGui.otherButtonsTopPanel.refreshWidgets();
			}
		}
	}

	public static void createObject(long id, long parent, QuestObjectType type, CompoundTag nbt, CompoundTag extra, UUID creator) {
		ClientQuestFile file = ClientQuestFile.INSTANCE;

		QuestObjectBase object = file.create(id, type, parent, extra);
		object.readData(nbt, FTBQuestsClient.holderLookup());
		file.getTranslationManager().processInitialTranslation(extra, object);
		object.onCreated();
		file.refreshIDMap();
		object.editedFromGUI();
		FTBQuests.getRecipeModHelper().refreshRecipes(object);

		LocalPlayer player = Minecraft.getInstance().player;
		if (object instanceof QuestObject qo && player != null && creator.equals(player.getUUID())) {
			file.getQuestScreen()
					.ifPresent(questScreen -> questScreen.open(qo, true));
		}

		QuestObjectUpdateListener listener = ClientUtils.getCurrentGuiAs(QuestObjectUpdateListener.class);

		if (listener != null) {
			listener.onQuestObjectUpdate(object);
		}
	}

	public static void createOtherTeamData(TeamDataUpdate dataUpdate) {
		if (ClientQuestFile.INSTANCE != null) {
			TeamData data = new TeamData(dataUpdate.uuid(), ClientQuestFile.INSTANCE, dataUpdate.name());
			ClientQuestFile.INSTANCE.addData(data, true);
		}
	}

	public static void teamDataChanged(TeamDataUpdate oldDataUpdate, TeamDataUpdate newDataUpdate) {
		if (ClientQuestFile.INSTANCE != null) {
			TeamData data = new TeamData(newDataUpdate.uuid(), ClientQuestFile.INSTANCE, newDataUpdate.name());
			ClientQuestFile.INSTANCE.addData(data, false);
		}
	}

	public static void deleteObject(long id) {
		QuestObjectBase object = ClientQuestFile.INSTANCE.getBase(id);

		if (object != null) {
			object.deleteChildren();
			object.deleteSelf();
//			ClientQuestFile.INSTANCE.refreshIDMap();
			object.editedFromGUI();
			FTBQuests.getRecipeModHelper().refreshRecipes(object);
			ClientQuestFile.INSTANCE.getTranslationManager().removeAllTranslations(object);
		}
	}

	public static void displayCompletionToast(long id) {
		QuestObject object = ClientQuestFile.INSTANCE.get(id);

		if (object != null) {
			Minecraft.getInstance().getToasts().addToast(new ToastQuestObject(object));
		}

		QuestScreen questScreen = ClientUtils.getCurrentGuiAs(QuestScreen.class);
		if (questScreen != null) {
			questScreen.refreshQuestPanel();
			questScreen.refreshChapterPanel();
			questScreen.refreshViewQuestPanel();
		}
	}

	public static void displayItemRewardToast(ItemStack stack, int count, boolean disableBlur) {
		ItemStack stack1 = ItemStackHooks.copyWithCount(stack, 1);
		Icon icon = ItemIcon.getItemIcon(stack1);

		if (!IRewardListenerScreen.add(new RewardKey(stack.getHoverName().getString(), icon, stack1, disableBlur), count)) {
			MutableComponent comp = count > 1 ?
					Component.literal(count + "x ").append(stack.getHoverName()) :
					stack.getHoverName().copy();
			Minecraft.getInstance().getToasts().addToast(new RewardToast(comp.withStyle(stack.getRarity().color()), icon));
		}
	}

	public static void displayRewardToast(long id, Component text, Icon icon, boolean disableBlur) {
		Icon i = icon.isEmpty() ? ClientQuestFile.INSTANCE.getBase(id).getIcon() : icon;

		if (!IRewardListenerScreen.add(new RewardKey(text.getString(), i, disableBlur), 1)) {
			Minecraft.getInstance().getToasts().addToast(new RewardToast(text, i));
		}
	}

	public static void editObject(long id, CompoundTag nbt) {
//		ClientQuestFile.INSTANCE.clearCachedData();
		QuestObjectBase object = ClientQuestFile.INSTANCE.getBase(id);

		if (object != null) {
			object.readData(nbt, FTBQuestsClient.holderLookup());
			object.editedFromGUI();
			object.clearCachedData();
			FTBQuests.getRecipeModHelper().refreshRecipes(object);
		}
	}

	public static void moveChapter(long id, boolean movingUp) {
		Chapter chapter = ClientQuestFile.INSTANCE.getChapter(id);

		if (chapter != null && chapter.getGroup().moveChapterWithinGroup(chapter, movingUp)) {
			ClientQuestFile.INSTANCE.clearCachedData();
			QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);
			if (gui != null) {
				gui.refreshChapterPanel();
			}
		}
	}

	public static void moveQuest(long id, long chapter, double x, double y) {
		if (ClientQuestFile.INSTANCE.get(id) instanceof Movable movable) {
			movable.onMoved(x, y, chapter);
			QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);
			if (gui != null) {
				gui.questPanel.withPreservedPos(Panel::refreshWidgets);
			}
		}
	}

	public static void syncEditingMode(UUID teamId, boolean editingMode) {
		if (ClientQuestFile.INSTANCE.getOrCreateTeamData(teamId).setCanEdit(Minecraft.getInstance().player, editingMode)) {
			setEditorPermission(editingMode);
			ClientQuestFile.INSTANCE.refreshGui();
		}
	}

	public static void togglePinned(long id, boolean pinned) {
		TeamData data = FTBQuestsClient.getClientPlayerData();
		data.setQuestPinned(Minecraft.getInstance().player, id, pinned);

		ClientQuestFile.INSTANCE.getQuestScreen().ifPresent(questScreen -> {
			questScreen.otherButtonsTopPanel.refreshWidgets();
			questScreen.refreshViewQuestPanel();
		});
	}

	public static void updateTeamData(UUID teamId, String name) {
		TeamData data = ClientQuestFile.INSTANCE.getOrCreateTeamData(teamId);
		data.setName(name);
	}

	public static void updateTaskProgress(UUID teamId, long task, long progress) {
		Task t = ClientQuestFile.INSTANCE.getTask(task);

		if (t != null) {
			TeamData data = ClientQuestFile.INSTANCE.getOrCreateTeamData(teamId);
			ClientQuestFile.INSTANCE.clearCachedProgress();
			data.setProgress(t, progress);
		}
	}

	public static void changeChapterGroup(long id, long newGroupId) {
		Chapter chapter = ClientQuestFile.INSTANCE.getChapter(id);

		if (chapter != null) {
			ChapterGroup newGroup = ClientQuestFile.INSTANCE.getChapterGroup(newGroupId);

			if (chapter.getGroup() != newGroup) {
				chapter.getGroup().removeChapter(chapter);
				newGroup.addChapter(chapter);
				chapter.file.clearCachedData();
				chapter.editedFromGUI();
			}
		}
	}

	public static void moveChapterGroup(long id, boolean movingUp) {
		ClientQuestFile.INSTANCE.moveChapterGroup(id, movingUp);
	}

	public static void objectStarted(UUID teamId, long id, @Nullable Date time) {
		TeamData teamData = ClientQuestFile.INSTANCE.getOrCreateTeamData(teamId);
		teamData.setStarted(id, time);

		refreshQuestScreenIfOpen();
	}

	public static void objectCompleted(UUID teamId, long id, @Nullable Date time) {
		TeamData teamData = ClientQuestFile.INSTANCE.getOrCreateTeamData(teamId);
		teamData.setCompleted(id, time);

		refreshQuestScreenIfOpen();

		FTBQuests.getRecipeModHelper().refreshRecipes(ClientQuestFile.INSTANCE.get(id));
	}

	public static void syncLock(UUID id, boolean lock) {
		if (ClientQuestFile.INSTANCE.getOrCreateTeamData(id).setLocked(lock)) {
			ClientQuestFile.INSTANCE.refreshGui();
		}
	}

	public static void resetReward(UUID teamId, UUID player, long rewardId) {
		Reward reward = ClientQuestFile.INSTANCE.getReward(rewardId);
        if (reward != null) {
            TeamData teamData = ClientQuestFile.INSTANCE.getOrCreateTeamData(teamId);

            if (teamData.resetReward(player, reward)) {
                refreshQuestScreenIfOpen();
            }
        }
    }

	private static void refreshQuestScreenIfOpen() {
		QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);
		if (gui != null) {
			gui.refreshChapterPanel();
			gui.refreshViewQuestPanel();
		}
	}

	public static void toggleChapterPinned(boolean pinned) {
		ClientQuestFile.INSTANCE.selfTeamData.setChapterPinned(FTBQuestsClient.getClientPlayer(), pinned);
		ClientQuestFile.INSTANCE.getQuestScreen().ifPresent(QuestScreen::refreshChapterPanel);
	}

	public static void syncRewardBlocking(UUID teamId, boolean rewardsBlocked) {
		if (ClientQuestFile.INSTANCE.getOrCreateTeamData(teamId).setRewardsBlocked(rewardsBlocked)) {
			ClientQuestFile.INSTANCE.refreshGui();
		}
	}

	public static void setEditorPermission(boolean hasPermission) {
		if (ClientQuestFile.exists()) {
			ClientQuestFile.INSTANCE.setEditorPermission(hasPermission);
		}
	}
}
