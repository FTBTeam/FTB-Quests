package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.util.ClientUtils;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.FTBQuestsNetCommon;
import dev.ftb.mods.ftbquests.gui.*;
import dev.ftb.mods.ftbquests.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.integration.FTBQuestsJEIHelper;
import dev.ftb.mods.ftbquests.net.TeamDataUpdate;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

public class FTBQuestsNetClient extends FTBQuestsNetCommon {
	@Override
	public void syncTeamData(boolean self, TeamData data) {
		data.file = ClientQuestFile.INSTANCE;
		data.file.addData(data, true);

		if (self) {
			ClientQuestFile.INSTANCE.self = data;
		}
	}

	@Override
	public void claimReward(UUID teamId, UUID player, long rewardId) {
		Reward reward = ClientQuestFile.INSTANCE.getReward(rewardId);

		if (reward == null) {
			return;
		}

		TeamData data = ClientQuestFile.INSTANCE.getData(teamId);
		data.claimReward(player, reward, System.currentTimeMillis());

		if (data == ClientQuestFile.INSTANCE.self) {
			QuestScreen treeGui = ClientUtils.getCurrentGuiAs(QuestScreen.class);

			if (treeGui != null) {
				treeGui.viewQuestPanel.refreshWidgets();
				treeGui.otherButtonsTopPanel.refreshWidgets();
			}
		}
	}

	@Override
	public void createObject(long id, long parent, QuestObjectType type, CompoundTag nbt, @Nullable CompoundTag extra) {
		QuestObjectBase object = ClientQuestFile.INSTANCE.create(type, parent, extra == null ? new CompoundTag() : extra);
		object.readData(nbt);
		object.id = id;
		object.onCreated();
		ClientQuestFile.INSTANCE.refreshIDMap();
		object.editedFromGUI();
		FTBQuestsJEIHelper.refresh(object);

		if (object instanceof Chapter) {
			ClientQuestFile.INSTANCE.questScreen.selectChapter((Chapter) object);
		}

		QuestObjectUpdateListener listener = ClientUtils.getCurrentGuiAs(QuestObjectUpdateListener.class);

		if (listener != null) {
			listener.onQuestObjectUpdate(object);
		}
	}

	@Override
	public void createOtherTeamData(TeamDataUpdate dataUpdate) {
		if (ClientQuestFile.INSTANCE != null) {
			TeamData data = new TeamData(dataUpdate.uuid);
			data.file = ClientQuestFile.INSTANCE;
			data.name = dataUpdate.name;
			data.file.addData(data, true);
		}
	}

	@Override
	public void teamDataChanged(TeamDataUpdate oldDataUpdate, TeamDataUpdate newDataUpdate) {
		if (ClientQuestFile.INSTANCE != null) {
			TeamData data = new TeamData(newDataUpdate.uuid);
			data.file = ClientQuestFile.INSTANCE;
			data.name = newDataUpdate.name;
			data.file.addData(data, false);
		}
	}

	@Override
	public void deleteObject(long id) {
		QuestObjectBase object = ClientQuestFile.INSTANCE.getBase(id);

		if (object != null) {
			object.deleteChildren();
			object.deleteSelf();
			ClientQuestFile.INSTANCE.refreshIDMap();
			object.editedFromGUI();
			FTBQuestsJEIHelper.refresh(object);
		}
	}

	@Override
	public void displayCompletionToast(long id) {
		QuestObject object = ClientQuestFile.INSTANCE.get(id);

		if (object != null) {
			Minecraft.getInstance().getToasts().addToast(new ToastQuestObject(object));
		}

		ClientQuestFile.INSTANCE.questScreen.questPanel.refreshWidgets();
		ClientQuestFile.INSTANCE.questScreen.chapterPanel.refreshWidgets();
		ClientQuestFile.INSTANCE.questScreen.viewQuestPanel.refreshWidgets();
	}

	@Override
	public void displayItemRewardToast(ItemStack stack, int count) {
		ItemStack stack1 = stack.copy();
		stack1.setCount(1);
		Icon icon = ItemIcon.getItemIcon(stack1);

		if (!IRewardListenerScreen.add(new RewardKey(stack.getHoverName().getString(), icon).setStack(stack1), count)) {
			MutableComponent s = stack.getHoverName().copy();

			if (count > 1) {
				s = Component.literal(count + "x ").append(s);
			}

			s.withStyle(stack.getRarity().color);

			Minecraft.getInstance().getToasts().addToast(new RewardToast(s, icon));
		}
	}

	@Override
	public void displayRewardToast(long id, Component text, Icon icon) {
		Icon i = icon.isEmpty() ? ClientQuestFile.INSTANCE.getBase(id).getIcon() : icon;

		if (!IRewardListenerScreen.add(new RewardKey(text.getString(), i), 1)) {
			Minecraft.getInstance().getToasts().addToast(new RewardToast(text, i));
		}
	}

	@Override
	public void editObject(long id, CompoundTag nbt) {
		ClientQuestFile.INSTANCE.clearCachedData();
		QuestObjectBase object = ClientQuestFile.INSTANCE.getBase(id);

		if (object != null) {
			object.readData(nbt);
			object.editedFromGUI();
			FTBQuestsJEIHelper.refresh(object);
		}
	}

	@Override
	public void moveChapter(long id, boolean up) {
		Chapter chapter = ClientQuestFile.INSTANCE.getChapter(id);

		if (chapter != null) {
			int index = chapter.group.chapters.indexOf(chapter);

			if (index != -1 && up ? (index > 0) : (index < chapter.group.chapters.size() - 1)) {
				chapter.group.chapters.remove(index);
				chapter.group.chapters.add(up ? index - 1 : index + 1, chapter);
				ClientQuestFile.INSTANCE.clearCachedData();

				QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);

				if (gui != null) {
					gui.chapterPanel.refreshWidgets();
				}
			}
		}
	}

	@Override
	public void moveQuest(long id, long chapter, double x, double y) {
		if (ClientQuestFile.INSTANCE.get(id) instanceof Movable movable) {
			movable.onMoved(x, y, chapter);
			QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);

			if (gui != null) {
				double sx = gui.questPanel.centerQuestX;
				double sy = gui.questPanel.centerQuestY;
				gui.questPanel.refreshWidgets();
				gui.questPanel.scrollTo(sx, sy);
			}
		}
	}

	@Override
	public void syncEditingMode(UUID teamId, boolean editingMode) {
		if (ClientQuestFile.INSTANCE.getData(teamId).setCanEdit(editingMode)) {
			ClientQuestFile.INSTANCE.refreshGui();
		}
	}

	@Override
	public void togglePinned(long id, boolean pinned) {
		TeamData data = FTBQuests.PROXY.getClientPlayerData();
		data.setQuestPinned(id, pinned);

		ClientQuestFile.INSTANCE.questScreen.otherButtonsBottomPanel.refreshWidgets();

		if (ClientQuestFile.INSTANCE.questScreen.viewQuestPanel != null) {
			ClientQuestFile.INSTANCE.questScreen.viewQuestPanel.refreshWidgets();
		}
	}

	@Override
	public void updateTeamData(UUID teamId, String name) {
		TeamData data = ClientQuestFile.INSTANCE.getData(teamId);
		data.name = name;
	}

	@Override
	public void updateTaskProgress(UUID teamId, long task, long progress) {
		Task t = ClientQuestFile.INSTANCE.getTask(task);

		if (t != null) {
			TeamData data = ClientQuestFile.INSTANCE.getData(teamId);
			ClientQuestFile.INSTANCE.clearCachedProgress();
			data.setProgress(t, progress);
		}
	}

	@Override
	public void changeChapterGroup(long id, long group) {
		Chapter chapter = ClientQuestFile.INSTANCE.getChapter(id);

		if (chapter != null) {
			ChapterGroup g = ClientQuestFile.INSTANCE.getChapterGroup(group);

			if (chapter.group != g) {
				chapter.group.chapters.remove(chapter);
				chapter.group = g;
				g.chapters.add(chapter);
				chapter.file.clearCachedData();
				chapter.editedFromGUI();
			}
		}
	}

	@Override
	public void moveChapterGroup(long id, boolean up) {
		ChapterGroup group = ClientQuestFile.INSTANCE.getChapterGroup(id);

		if (!group.isDefaultGroup()) {
			int index = group.file.chapterGroups.indexOf(group);

			if (index != -1 && up ? (index > 1) : (index < group.file.chapterGroups.size() - 1)) {
				group.file.chapterGroups.remove(index);
				group.file.chapterGroups.add(up ? index - 1 : index + 1, group);
				ClientQuestFile.INSTANCE.clearCachedData();

				QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);

				if (gui != null) {
					gui.chapterPanel.refreshWidgets();
				}
			}
		}
	}

	@Override
	public void objectStarted(UUID teamId, long id, @Nullable Date time) {
		TeamData teamData = ClientQuestFile.INSTANCE.getData(teamId);
		teamData.setStarted(id, time);

		QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);

		if (gui != null) {
			gui.chapterPanel.refreshWidgets();

			if (gui.viewQuestPanel != null) {
				gui.viewQuestPanel.refreshWidgets();
			}
		}
	}

	@Override
	public void objectCompleted(UUID teamId, long id, @Nullable Date time) {
		TeamData teamData = ClientQuestFile.INSTANCE.getData(teamId);
		teamData.setCompleted(id, time);

		QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);

		if (gui != null) {
			gui.chapterPanel.refreshWidgets();

			if (gui.viewQuestPanel != null) {
				gui.viewQuestPanel.refreshWidgets();
			}
		}

		FTBQuestsJEIHelper.refresh(ClientQuestFile.INSTANCE.get(id));
	}

	@Override
	public void syncLock(UUID id, boolean lock) {
		if (ClientQuestFile.INSTANCE.getData(id).setLocked(lock)) {
			ClientQuestFile.INSTANCE.refreshGui();
		}
	}

	@Override
	public void resetReward(UUID teamId, UUID player, long rewardId) {
		Reward reward = ClientQuestFile.INSTANCE.getReward(rewardId);

		if (reward == null) {
			return;
		}

		TeamData teamData = ClientQuestFile.INSTANCE.getData(teamId);

		if (teamData.resetReward(player, reward)) {
			QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);

			if (gui != null) {
				gui.chapterPanel.refreshWidgets();

				if (gui.viewQuestPanel != null) {
					gui.viewQuestPanel.refreshWidgets();
				}
			}
		}
	}

	@Override
	public void toggleChapterPinned(boolean pinned) {
		ClientQuestFile.INSTANCE.self.setChapterPinned(pinned);
		ClientQuestFile.INSTANCE.questScreen.chapterPanel.refreshWidgets();
	}

	@Override
	public void syncRewardBlocking(UUID uuid, boolean rewardsBlocked) {
		if (ClientQuestFile.INSTANCE.getData(uuid).setRewardsBlocked(rewardsBlocked)) {
			ClientQuestFile.INSTANCE.refreshGui();
		}
	}
}
