package dev.ftb.mods.ftbquests.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import dev.architectury.hooks.item.ItemStackHooks;

import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.gui.IRewardListenerScreen;
import dev.ftb.mods.ftbquests.client.gui.QuestObjectUpdateListener;
import dev.ftb.mods.ftbquests.client.gui.RewardKey;
import dev.ftb.mods.ftbquests.client.gui.RewardToast;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.ftb.mods.ftbquests.events.ObjectStartedEvent;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.net.TeamDataUpdate;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.ToastReward;
import dev.ftb.mods.ftbquests.quest.task.Task;

import java.util.Date;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public class FTBQuestsNetClient {
	public static void syncTeamData(TeamData data) {
		ClientQuestFile.getInstance().addData(data, true);
		ClientQuestFile.getInstance().selfTeamData = data;
	}

	public static void claimReward(UUID teamId, UUID player, long rewardId) {
		Reward reward = ClientQuestFile.getInstance().getReward(rewardId);

		if (reward == null) {
			return;
		}

		TeamData data = ClientQuestFile.getInstance().getOrCreateTeamData(teamId);
		data.markRewardAsClaimed(player, reward, System.currentTimeMillis());

		if (data == ClientQuestFile.getInstance().selfTeamData) {
			QuestScreen treeGui = ClientUtils.getCurrentGuiAs(QuestScreen.class);
			if (treeGui != null) {
				treeGui.refreshViewQuestPanel();
				treeGui.otherButtonsTopPanel.refreshWidgets();
			}
		}
	}

	public static void createObject(long id, long parent, QuestObjectType type, CompoundTag nbt, CompoundTag extra, UUID creator) {
		ClientQuestFile file = ClientQuestFile.getInstance();

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
		if (ClientQuestFile.exists()) {
			TeamData data = new TeamData(dataUpdate.uuid(), false, dataUpdate.name());
			ClientQuestFile.getInstance().addData(data, true);
		}
	}

	public static void teamDataChanged(TeamDataUpdate dataUpdate) {
		if (ClientQuestFile.exists()) {
			TeamData data = new TeamData(dataUpdate.uuid(), false, dataUpdate.name());
			ClientQuestFile.getInstance().addData(data, false);
		}
	}

	public static void deleteObject(long id) {
		QuestObjectBase object = ClientQuestFile.getInstance().getBase(id);

		if (object != null) {
			object.deleteChildren();
			object.deleteSelf();
			ClientQuestFile.getInstance().refreshIDMap();
			object.editedFromGUI();
			FTBQuests.getRecipeModHelper().refreshRecipes(object);
			ClientQuestFile.getInstance().getTranslationManager().removeAllTranslations(object);
		}
	}

	public static void notifyPlayerOfCompletion(long id) {
		if (FTBQuestsClientConfig.COMPLETION_STYLE.get().notifyCompletion(id)) {
			QuestScreen questScreen = ClientUtils.getCurrentGuiAs(QuestScreen.class);
			if (questScreen != null) {
				questScreen.refreshQuestPanel();
				questScreen.refreshChapterPanel();
				questScreen.refreshViewQuestPanel();
			}
		}
	}

	public static void displayItemRewardToast(ItemStack stack, int count, boolean disableBlur) {
		ItemStack stack1 = ItemStackHooks.copyWithCount(stack, 1);
		Icon<?> icon = ItemIcon.ofItemStack(stack1);

		if (!IRewardListenerScreen.add(new RewardKey(stack.getHoverName().getString(), icon, stack1, disableBlur), count)) {
			MutableComponent comp = count > 1 ?
					Component.literal(count + "x ").append(stack.getHoverName()) :
					stack.getHoverName().copy();
			FTBQuestsClientConfig.REWARD_STYLE.get().notifyReward(comp.withStyle(stack.getRarity().color()), icon);
		}
	}

	public static void displayRewardToast(long id, Component text, Icon<?> icon, boolean disableBlur) {
		Icon<?> actualIcon = icon.isEmpty() ? getFallbackIcon(id) : icon;

		if (!IRewardListenerScreen.add(new RewardKey(text.getString(), actualIcon, disableBlur), 1)) {
			FTBQuestsClientConfig.REWARD_STYLE.get().notifyReward(text, actualIcon);
		}
	}

	private static Icon<?> getFallbackIcon(long id) {
		return ClientQuestFile.getInstance().getBase(id) instanceof QuestObjectBase qo ? qo.getIcon() : Icon.empty();
	}

	public static void editObject(long id, CompoundTag nbt) {
		ClientQuestFile.getInstance().clearCachedData();
		QuestObjectBase object = ClientQuestFile.getInstance().getBase(id);

		if (object != null) {
			object.readData(nbt, FTBQuestsClient.holderLookup());
			object.editedFromGUI();
			FTBQuests.getRecipeModHelper().refreshRecipes(object);
		}
	}

	public static void moveChapter(long id, boolean movingUp) {
		Chapter chapter = ClientQuestFile.getInstance().getChapter(id);

		if (chapter != null && chapter.getGroup().moveChapterWithinGroup(chapter, movingUp)) {
			ClientQuestFile.getInstance().clearCachedData();
			QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);
			if (gui != null) {
				gui.refreshChapterPanel();
			}
		}
	}

	public static void moveQuest(long id, long chapter, double x, double y) {
		if (ClientQuestFile.getInstance().getBase(id) instanceof Movable movable) {
			movable.onMoved(x, y, chapter);
			QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);
			if (gui != null) {
				gui.questPanel.withPreservedPos(Panel::refreshWidgets);
			}
		}
	}

	public static void syncEditingMode(UUID teamId, boolean editingMode) {
		if (ClientQuestFile.getInstance().getOrCreateTeamData(teamId).setCanEdit(ClientUtils.getClientPlayer(), editingMode)) {
			setEditorPermission(editingMode);
			ClientQuestFile.getInstance().refreshGui();
		}
	}

	public static void togglePinned(long id, boolean pinned) {
		TeamData data = FTBQuestsClient.getClientPlayerData();
		data.setQuestPinned(ClientUtils.getClientPlayer(), id, pinned);

		ClientQuestFile.getInstance().getQuestScreen().ifPresent(questScreen -> {
			questScreen.otherButtonsTopPanel.refreshWidgets();
			questScreen.refreshViewQuestPanel();
		});
	}

	public static void updateTeamData(UUID teamId, String name) {
		TeamData data = ClientQuestFile.getInstance().getOrCreateTeamData(teamId);
		data.setName(name);
	}

	public static void updateTaskProgress(UUID teamId, long task, long progress) {
		Task t = ClientQuestFile.getInstance().getTask(task);

		if (t != null) {
			TeamData data = ClientQuestFile.getInstance().getOrCreateTeamData(teamId);
			ClientQuestFile.getInstance().clearCachedProgress();
			data.setProgress(t, progress);
		}
	}

	public static void changeChapterGroup(long id, long newGroupId) {
		Chapter chapter = ClientQuestFile.getInstance().getChapter(id);

		if (chapter != null) {
			ChapterGroup newGroup = ClientQuestFile.getInstance().getChapterGroup(newGroupId);

			if (chapter.getGroup() != newGroup) {
				chapter.getGroup().removeChapter(chapter);
				newGroup.addChapter(chapter);
				chapter.file.clearCachedData();
				chapter.editedFromGUI();
			}
		}
	}

	public static void moveChapterGroup(long id, boolean movingUp) {
		ClientQuestFile.getInstance().moveChapterGroup(id, movingUp);
	}

	public static void objectStarted(UUID teamId, long id, @Nullable Date time) {
		TeamData teamData = ClientQuestFile.getInstance().getOrCreateTeamData(teamId);
		teamData.setStarted(id, time);

		refreshQuestScreenIfOpen();

		fireStartedEvent(teamData, id, time);
	}

	public static void objectCompleted(UUID teamId, long id, @Nullable Date time) {
		TeamData teamData = ClientQuestFile.getInstance().getOrCreateTeamData(teamId);
		teamData.setCompleted(id, time);

		refreshQuestScreenIfOpen();

		QuestObject qo = ClientQuestFile.getInstance().get(id);
		if (qo != null) {
			FTBQuests.getRecipeModHelper().refreshRecipes(qo);
		}

		fireCompletedEvent(teamData, id, time);
	}

	private static void fireStartedEvent(TeamData teamData, long id, @Nullable Date time) {
		switch (ClientQuestFile.getInstance().get(id)) {
			case Quest q -> {
				QuestProgressEventData<Quest> eventData = QuestProgressEventData.forClient(time, teamData, q);
				ObjectStartedEvent.QUEST.invoker().act(new ObjectStartedEvent.QuestEvent(eventData));
			}
			case Chapter c -> {
				QuestProgressEventData<Chapter> eventData = QuestProgressEventData.forClient(time, teamData, c);
				ObjectStartedEvent.CHAPTER.invoker().act(new ObjectStartedEvent.ChapterEvent(eventData));
			}
			case Task t -> {
				QuestProgressEventData<Task> eventData = QuestProgressEventData.forClient(time, teamData, t);
				ObjectStartedEvent.TASK.invoker().act(new ObjectStartedEvent.TaskEvent(eventData));
			}
			case BaseQuestFile f -> {
				QuestProgressEventData<BaseQuestFile> eventData = QuestProgressEventData.forClient(time, teamData, f);
				ObjectStartedEvent.FILE.invoker().act(new ObjectStartedEvent.FileEvent(eventData));
			}
			case null, default -> {}
		}
	}

	private static void fireCompletedEvent(TeamData teamData, long id, @Nullable Date time) {
		switch (ClientQuestFile.getInstance().get(id)) {
			case Quest q -> {
				QuestProgressEventData<Quest> eventData = QuestProgressEventData.forClient(time, teamData, q);
				ObjectCompletedEvent.QUEST.invoker().act(new ObjectCompletedEvent.QuestEvent(eventData));
			}
			case Chapter c -> {
				QuestProgressEventData<Chapter> eventData = QuestProgressEventData.forClient(time, teamData, c);
				ObjectCompletedEvent.CHAPTER.invoker().act(new ObjectCompletedEvent.ChapterEvent(eventData));
			}
			case Task t -> {
				QuestProgressEventData<Task> eventData = QuestProgressEventData.forClient(time, teamData, t);
				ObjectCompletedEvent.TASK.invoker().act(new ObjectCompletedEvent.TaskEvent(eventData));
			}
			case BaseQuestFile f -> {
				QuestProgressEventData<BaseQuestFile> eventData = QuestProgressEventData.forClient(time, teamData, f);
				ObjectCompletedEvent.FILE.invoker().act(new ObjectCompletedEvent.FileEvent(eventData));
			}
			case null, default -> {}
		}
	}

	public static void syncLock(UUID id, boolean lock) {
		if (ClientQuestFile.getInstance().getOrCreateTeamData(id).setLocked(lock)) {
			ClientQuestFile.getInstance().refreshGui();
		}
	}

	public static void resetReward(UUID teamId, UUID player, long rewardId) {
		Reward reward = ClientQuestFile.getInstance().getReward(rewardId);
        if (reward != null) {
            TeamData teamData = ClientQuestFile.getInstance().getOrCreateTeamData(teamId);

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

	public static void syncRewardBlocking(UUID teamId, boolean rewardsBlocked) {
		if (ClientQuestFile.getInstance().getOrCreateTeamData(teamId).setRewardsBlocked(rewardsBlocked)) {
			ClientQuestFile.getInstance().refreshGui();
		}
	}

	public static void setEditorPermission(boolean hasPermission) {
		if (ClientQuestFile.exists()) {
			ClientQuestFile.getInstance().setEditorPermission(hasPermission);
		}
	}

	public static void displayCustomToast(ToastReward t) {
		Minecraft.getInstance().getToastManager().addToast(new RewardToast(t.getTitle(), Component.translatable(t.getDescription()), t.getIcon()));
	}
}
