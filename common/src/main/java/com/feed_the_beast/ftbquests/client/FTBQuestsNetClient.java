package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsNetCommon;
import com.feed_the_beast.ftbquests.gui.IRewardListenerGui;
import com.feed_the_beast.ftbquests.gui.RewardKey;
import com.feed_the_beast.ftbquests.gui.RewardToast;
import com.feed_the_beast.ftbquests.gui.ToastQuestObject;
import com.feed_the_beast.ftbquests.gui.quests.GuiQuests;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

public class FTBQuestsNetClient extends FTBQuestsNetCommon
{
	@Override
	public void changeProgress(UUID player, long id, ChangeProgress type, boolean notifications)
	{
		QuestObjectBase object = ClientQuestFile.INSTANCE.getBase(id);

		if (object != null)
		{
			object.forceProgress(ClientQuestFile.INSTANCE.getData(player), type, notifications);
		}
	}

	@Override
	public void claimReward(UUID player, long id)
	{
		Reward reward = ClientQuestFile.INSTANCE.getReward(id);

		if (reward == null)
		{
			return;
		}

		PlayerData data = ClientQuestFile.INSTANCE.getData(player);
		data.setRewardClaimed(reward.id, true);

		if (data == ClientQuestFile.INSTANCE.self)
		{
			GuiQuests treeGui = ClientUtils.getCurrentGuiAs(GuiQuests.class);

			if (treeGui != null)
			{
				treeGui.viewQuestPanel.refreshWidgets();
				treeGui.otherButtonsTopPanel.refreshWidgets();
			}
		}
	}

	@Override
	public void createObject(long id, long parent, QuestObjectType type, CompoundTag nbt, @Nullable CompoundTag extra)
	{
		QuestObjectBase object = ClientQuestFile.INSTANCE.create(type, parent, extra == null ? new CompoundTag() : extra);
		object.readData(nbt);
		object.id = id;
		object.onCreated();
		ClientQuestFile.INSTANCE.refreshIDMap();
		object.editedFromGUI();
		FTBQuestsJEIHelper.refresh(object);

		if (object instanceof Chapter)
		{
			ClientQuestFile.INSTANCE.questTreeGui.selectChapter((Chapter) object);
		}
	}

	@Override
	public void createPlayerData(UUID uuid, String name)
	{
		PlayerData data = new PlayerData(ClientQuestFile.INSTANCE, uuid);
		data.name = name;
		data.file.addData(data, true);
	}

	@Override
	public void deleteObject(long id)
	{
		QuestObjectBase object = ClientQuestFile.INSTANCE.getBase(id);

		if (object != null)
		{
			object.deleteChildren();
			object.deleteSelf();
			ClientQuestFile.INSTANCE.refreshIDMap();
			object.editedFromGUI();
			FTBQuestsJEIHelper.refresh(object);
		}
	}

	@Override
	public void displayCompletionToast(long id)
	{
		QuestObject object = ClientQuestFile.INSTANCE.get(id);

		if (object != null)
		{
			Minecraft.getInstance().getToasts().addToast(new ToastQuestObject(object));
		}

		ClientQuestFile.INSTANCE.questTreeGui.questPanel.refreshWidgets();
		ClientQuestFile.INSTANCE.questTreeGui.chapterPanel.refreshWidgets();
		ClientQuestFile.INSTANCE.questTreeGui.viewQuestPanel.refreshWidgets();
	}

	@Override
	public void displayItemRewardToast(ItemStack stack, int count)
	{
		ItemStack stack1 = stack.copy();
		stack1.setCount(1);
		Icon icon = ItemIcon.getItemIcon(stack1);

		if (!IRewardListenerGui.add(new RewardKey(stack.getHoverName().getString(), icon).setStack(stack1), count))
		{
			MutableComponent s = stack.getHoverName().copy();

			if (count > 1)
			{
				s = new TextComponent(count + "x ").append(s);
			}

			s.withStyle(stack.getRarity().color);

			Minecraft.getInstance().getToasts().addToast(new RewardToast(s, icon));
		}
	}

	@Override
	public void displayRewardToast(long id, Component text, Icon icon)
	{
		Icon i = icon.isEmpty() ? ClientQuestFile.INSTANCE.getBase(id).getIcon() : icon;

		if (!IRewardListenerGui.add(new RewardKey(text.getString(), i), 1))
		{
			Minecraft.getInstance().getToasts().addToast(new RewardToast(text, i));
		}
	}

	@Override
	public void editObject(long id, CompoundTag nbt)
	{
		ClientQuestFile.INSTANCE.clearCachedData();
		QuestObjectBase object = ClientQuestFile.INSTANCE.getBase(id);

		if (object != null)
		{
			object.readData(nbt);
			object.editedFromGUI();
			FTBQuestsJEIHelper.refresh(object);
		}
	}

	@Override
	public void moveChapter(long id, boolean up)
	{
		Chapter chapter = ClientQuestFile.INSTANCE.getChapter(id);

		if (chapter != null)
		{
			int index = chapter.group.chapters.indexOf(chapter);

			if (index != -1 && up ? (index > 0) : (index < chapter.group.chapters.size() - 1))
			{
				chapter.group.chapters.remove(index);
				chapter.group.chapters.add(up ? index - 1 : index + 1, chapter);
				ClientQuestFile.INSTANCE.clearCachedData();

				GuiQuests gui = ClientUtils.getCurrentGuiAs(GuiQuests.class);

				if (gui != null)
				{
					gui.chapterPanel.refreshWidgets();
					gui.chapterPanel.alignWidgets();
				}
			}
		}
	}

	@Override
	public void moveQuest(long id, long chapter, double x, double y)
	{
		Quest quest = ClientQuestFile.INSTANCE.getQuest(id);

		if (quest != null)
		{
			quest.moved(x, y, chapter);
			GuiQuests gui = ClientUtils.getCurrentGuiAs(GuiQuests.class);

			if (gui != null)
			{
				double sx = gui.questPanel.centerQuestX;
				double sy = gui.questPanel.centerQuestY;
				gui.questPanel.refreshWidgets();
				gui.questPanel.scrollTo(sx, sy);
			}
		}
	}

	@Override
	public void syncEditingMode(boolean editingMode)
	{
		if (ClientQuestFile.INSTANCE.self.setCanEdit(editingMode))
		{
			ClientQuestFile.INSTANCE.refreshGui();
		}
	}

	@Override
	public void togglePinned(long id)
	{
		PlayerData data = FTBQuests.PROXY.getClientPlayerData();
		data.setQuestPinned(id, !data.isQuestPinned(id));

		ClientQuestFile.INSTANCE.questTreeGui.otherButtonsBottomPanel.refreshWidgets();

		if (ClientQuestFile.INSTANCE.questTreeGui.viewQuestPanel != null)
		{
			ClientQuestFile.INSTANCE.questTreeGui.viewQuestPanel.refreshWidgets();
		}
	}

	@Override
	public void updatePlayerData(UUID uuid, String name)
	{
		PlayerData data = ClientQuestFile.INSTANCE.getData(uuid);
		data.name = name;
	}

	@Override
	public void updateTaskProgress(UUID player, long task, long progress)
	{
		Task t = ClientQuestFile.INSTANCE.getTask(task);

		if (t != null)
		{
			PlayerData data = ClientQuestFile.INSTANCE.getData(player);
			ClientQuestFile.INSTANCE.clearCachedProgress();
			data.getTaskData(t).setProgress(progress);
		}
	}
}