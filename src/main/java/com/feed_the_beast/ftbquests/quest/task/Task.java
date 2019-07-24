package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.WrappedIngredient;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.net.MessageDisplayCompletionToast;
import com.feed_the_beast.ftbquests.net.MessageSubmitTask;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenCore;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenPart;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class Task extends QuestObject
{
	public final Quest quest;

	public Task(Quest q)
	{
		quest = q;
	}

	@Override
	public final QuestObjectType getObjectType()
	{
		return QuestObjectType.TASK;
	}

	@Override
	public final QuestFile getQuestFile()
	{
		return quest.chapter.file;
	}

	@Override
	public final Chapter getQuestChapter()
	{
		return quest.chapter;
	}

	@Override
	public final int getParentID()
	{
		return quest.id;
	}

	public abstract TaskType getType();

	public abstract TaskData createData(QuestData data);

	@Override
	public final int getRelativeProgressFromChildren(QuestData data)
	{
		return data.getTaskData(this).getRelativeProgress();
	}

	@Override
	public final void onCompleted(QuestData data, List<EntityPlayerMP> notifyPlayers)
	{
		super.onCompleted(data, notifyPlayers);
		new ObjectCompletedEvent.TaskEvent(data, this).post();
		boolean questComplete = quest.isComplete(data);

		if (quest.tasks.size() > 1 && !questComplete && !disableToast)
		{
			new MessageDisplayCompletionToast(id).sendTo(notifyPlayers);
		}

		if (questComplete)
		{
			quest.onCompleted(data, notifyPlayers);
		}
	}

	public long getMaxProgress()
	{
		return 1L;
	}

	public String getMaxProgressString()
	{
		return StringUtils.formatDouble(getMaxProgress(), true);
	}

	@Override
	public final void changeProgress(QuestData data, ChangeProgress type)
	{
		data.getTaskData(this).setProgress(type.reset ? 0L : getMaxProgress());
	}

	@Override
	public final void deleteSelf()
	{
		quest.tasks.remove(this);

		for (QuestData data : quest.chapter.file.getAllData())
		{
			data.removeTask(this);
		}

		super.deleteSelf();
	}

	@Override
	public final void deleteChildren()
	{
		for (QuestData data : quest.chapter.file.getAllData())
		{
			data.removeTask(this);
		}

		super.deleteChildren();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void editedFromGUI()
	{
		GuiQuestTree gui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

		if (gui != null)
		{
			gui.questPanel.refreshWidgets();
			gui.viewQuestPanel.refreshWidgets();
		}
	}

	@Override
	public final void onCreated()
	{
		quest.tasks.add(this);

		for (QuestData data : quest.chapter.file.getAllData())
		{
			data.createTaskData(this);
		}
	}

	@Override
	public Icon getAltIcon()
	{
		return getType().getIcon();
	}

	@Override
	public String getAltTitle()
	{
		return getType().getDisplayName();
	}

	@Override
	public final ConfigGroup createSubGroup(ConfigGroup group)
	{
		TaskType type = getType();
		return group.getGroup(getObjectType().getID()).getGroup(type.getRegistryName().getNamespace()).getGroup(type.getRegistryName().getPath());
	}

	public Class<? extends TileTaskScreenCore> getScreenCoreClass()
	{
		return TileTaskScreenCore.class;
	}

	public Class<? extends TileTaskScreenPart> getScreenPartClass()
	{
		return TileTaskScreenPart.class;
	}

	public TileTaskScreenCore createScreenCore(World world)
	{
		return new TileTaskScreenCore();
	}

	public TileTaskScreenPart createScreenPart(World world)
	{
		return new TileTaskScreenPart();
	}

	public void drawGUI(@Nullable TaskData data, int x, int y, int w, int h)
	{
		getIcon().draw(x, y, w, h);
	}

	public void drawScreen(@Nullable TaskData data)
	{
		getIcon().draw3D(Icon.EMPTY);
	}

	public boolean canInsertItem()
	{
		return false;
	}

	public boolean consumesResources()
	{
		return canInsertItem();
	}

	public boolean hideProgressNumbers()
	{
		return getMaxProgress() <= 1L;
	}

	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list, @Nullable TaskData data)
	{
		if (consumesResources())
		{
			list.add("");
			list.add(TextFormatting.YELLOW.toString() + TextFormatting.UNDERLINE + I18n.format("ftbquests.task.click_to_submit"));
		}
	}

	@SideOnly(Side.CLIENT)
	public boolean addTitleInMouseOverText()
	{
		return true;
	}

	@SideOnly(Side.CLIENT)
	public void onButtonClicked(boolean canClick)
	{
		if (!autoSubmitOnPlayerTick())
		{
			if (canClick)
			{
				GuiHelper.playClickSound();
				new MessageSubmitTask(id).sendToServer();
			}
		}
	}

	public boolean submitItemsOnInventoryChange()
	{
		return false;
	}

	@Nullable
	public Object getIngredient()
	{
		if (addTitleInMouseOverText())
		{
			return getIcon().getIngredient();
		}

		return new WrappedIngredient(getIcon().getIngredient()).tooltip();
	}

	@Override
	public final int refreshJEI()
	{
		return FTBQuestsJEIHelper.QUESTS;
	}

	@SideOnly(Side.CLIENT)
	public String getButtonText()
	{
		return getMaxProgress() > 1L || consumesResources() ? getMaxProgressString() : "";
	}

	public boolean autoSubmitOnPlayerTick()
	{
		return false;
	}

	@Override
	public boolean cacheProgress()
	{
		return false;
	}
}