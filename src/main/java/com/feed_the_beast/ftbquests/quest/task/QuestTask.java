package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.gui.WrappedIngredient;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.net.MessageDisplayCompletionToast;
import com.feed_the_beast.ftbquests.net.MessageSubmitTask;
import com.feed_the_beast.ftbquests.quest.EnumChangeProgress;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenCore;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenPart;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestTask extends QuestObject
{
	public final Quest quest;

	public QuestTask(Quest q)
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
	public final QuestChapter getQuestChapter()
	{
		return quest.chapter;
	}

	@Override
	public final int getParentID()
	{
		return quest.id;
	}

	public abstract QuestTaskType getType();

	public abstract QuestTaskData createData(ITeamData data);

	@Override
	public final int getRelativeProgressFromChildren(ITeamData data)
	{
		return data.getQuestTaskData(this).getRelativeProgress();
	}

	@Override
	public final void onCompleted(ITeamData data, List<EntityPlayerMP> notifyPlayers)
	{
		super.onCompleted(data, notifyPlayers);
		new ObjectCompletedEvent.TaskEvent(data, this).post();
		boolean questComplete = quest.isComplete(data);

		if (quest.tasks.size() > 1 && !questComplete)
		{
			for (EntityPlayerMP player : notifyPlayers)
			{
				new MessageDisplayCompletionToast(id).sendTo(player);
			}
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
	public final void changeProgress(ITeamData data, EnumChangeProgress type)
	{
		QuestTaskData taskData = data.getQuestTaskData(this);

		taskData.changeProgress(type);

		if (type.reset)
		{
			taskData.isComplete = false;
		}

		taskData.sync();
	}

	@Override
	public final void deleteSelf()
	{
		quest.tasks.remove(this);

		for (ITeamData data : quest.chapter.file.getAllData())
		{
			data.removeTask(this);
		}

		super.deleteSelf();
	}

	@Override
	public final void deleteChildren()
	{
		for (ITeamData data : quest.chapter.file.getAllData())
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

		if (gui != null && gui.getViewedQuest() != null)
		{
			gui.viewQuestPanel.refreshWidgets();
		}

		if (gui != null)
		{
			gui.questPanel.refreshWidgets();
		}
	}

	@Override
	public final void onCreated()
	{
		quest.tasks.add(this);

		for (ITeamData data : quest.chapter.file.getAllData())
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
	public ITextComponent getAltDisplayName()
	{
		return getType().getDisplayName();
	}

	@Override
	public final ConfigGroup createSubGroup(ConfigGroup group)
	{
		QuestTaskType type = getType();
		return group.getGroup(getObjectType().getName()).getGroup(type.getRegistryName().getNamespace()).getGroup(type.getRegistryName().getPath());
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

	@SideOnly(Side.CLIENT)
	public void drawGUI(@Nullable QuestTaskData data, int x, int y, int w, int h)
	{
		getIcon().draw(x, y, w, h);
	}

	@SideOnly(Side.CLIENT)
	public void drawScreen(@Nullable QuestTaskData data)
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
	public void addMouseOverText(List<String> list, @Nullable QuestTaskData data)
	{
		if (consumesResources())
		{
			list.add("");
			list.add(TextFormatting.YELLOW.toString() + TextFormatting.UNDERLINE + I18n.format("ftbquests.task.click_to_submit"));
		}
	}

	public boolean addTitleInMouseOverText()
	{
		return true;
	}

	@SideOnly(Side.CLIENT)
	public void onButtonClicked(boolean canClick)
	{
		if (canClick)
		{
			new MessageSubmitTask(id).sendToServer();
		}
	}

	public boolean submitItemsOnInventoryChange()
	{
		return false;
	}

	@Nullable
	@SideOnly(Side.CLIENT)
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
}