package dev.ftb.mods.ftbquests.client;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.FTBQuestsNetCommon;
import dev.ftb.mods.ftbquests.gui.IRewardListenerScreen;
import dev.ftb.mods.ftbquests.gui.QuestObjectUpdateListener;
import dev.ftb.mods.ftbquests.gui.RewardKey;
import dev.ftb.mods.ftbquests.gui.RewardToast;
import dev.ftb.mods.ftbquests.gui.ToastQuestObject;
import dev.ftb.mods.ftbquests.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.integration.jei.FTBQuestsJEIHelper;
import dev.ftb.mods.ftbquests.net.MessageChangeChapterGroupResponse;
import dev.ftb.mods.ftbquests.quest.ChangeProgress;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.QuestKey;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;

public class FTBQuestsNetClient extends FTBQuestsNetCommon {
	@Override
	public void changeProgress(UUID teamId, long id, ChangeProgress type, boolean notifications) {
		QuestObjectBase object = ClientQuestFile.INSTANCE.getBase(id);

		if (object != null) {
			object.forceProgress(ClientQuestFile.INSTANCE.getData(teamId), type, notifications);
		}
	}

	@Override
	public void claimReward(QuestKey key) {
		Reward reward = ClientQuestFile.INSTANCE.getReward(key.id);

		if (reward == null) {
			return;
		}

		TeamData data = ClientQuestFile.INSTANCE.getData(key.uuid);
		data.claimReward(key.uuid, reward);

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
	public void createTeamData(UUID teamId, String name) {
		TeamData data = new TeamData(ClientQuestFile.INSTANCE, teamId);
		data.name = name;
		data.file.addData(data, true);
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
				s = new TextComponent(count + "x ").append(s);
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
		Quest quest = ClientQuestFile.INSTANCE.getQuest(id);

		if (quest != null) {
			quest.moved(x, y, chapter);
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
	public void syncEditingMode(boolean editingMode) {
		if (ClientQuestFile.INSTANCE.self.setCanEdit(editingMode)) {
			ClientQuestFile.INSTANCE.refreshGui();
		}
	}

	@Override
	public void togglePinned(long id) {
		TeamData data = FTBQuests.PROXY.getClientPlayerData();
		data.setQuestPinned(id, !data.isQuestPinned(id));

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
				new MessageChangeChapterGroupResponse(id, group).sendToAll();
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
	public void objectStarted(UUID teamId, long id, @Nullable Instant time) {
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
	public void objectCompleted(UUID teamId, long id, @Nullable Instant time) {
		TeamData teamData = ClientQuestFile.INSTANCE.getData(teamId);
		teamData.setCompleted(id, time);

		QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);

		if (gui != null) {
			gui.chapterPanel.refreshWidgets();

			if (gui.viewQuestPanel != null) {
				gui.viewQuestPanel.refreshWidgets();
			}
		}
	}
}