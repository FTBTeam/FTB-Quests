package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.net.MessageDisplayCompletionToast;
import com.feed_the_beast.ftbquests.net.MessageSubmitTask;
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
	public final long getProgress(ITeamData data)
	{
		return data.getQuestTaskData(this).getProgress();
	}

	@Override
	public final int getRelativeProgress(ITeamData data)
	{
		return data.getQuestTaskData(this).getRelativeProgress();
	}

	@Override
	public final boolean isComplete(ITeamData data)
	{
		long max = getMaxProgress();
		return max > 0L && getProgress(data) >= max;
	}

	@Override
	public final void onCompleted(ITeamData data, List<EntityPlayerMP> onlineMembers)
	{
		super.onCompleted(data, onlineMembers);
		new ObjectCompletedEvent.TaskEvent(data, this).post();
		boolean questComplete = quest.isComplete(data);

		if (quest.tasks.size() > 1 && !questComplete)
		{
			for (EntityPlayerMP player : onlineMembers)
			{
				new MessageDisplayCompletionToast(id).sendTo(player);
			}
		}

		if (questComplete)
		{
			quest.onCompleted(data, onlineMembers);
		}
	}

	@Override
	public long getMaxProgress()
	{
		return 1;
	}

	public String getMaxProgressString()
	{
		return StringUtils.formatDouble(getMaxProgress(), true);
	}

	@Override
	public final void resetProgress(ITeamData data, boolean dependencies)
	{
		QuestTaskData taskData = data.getQuestTaskData(this);
		taskData.resetProgress();
		taskData.isComplete = false;
		taskData.sync();
	}

	@Override
	public final void completeInstantly(ITeamData data, boolean dependencies)
	{
		QuestTaskData taskData = data.getQuestTaskData(this);
		taskData.completeInstantly();
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

		if (gui != null && gui.getSelectedQuest() != null)
		{
			gui.questLeft.refreshWidgets();
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

	public boolean hideProgressNumbers()
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list, @Nullable QuestTaskData data)
	{
		if (data != null && canInsertItem())
		{
			list.add(TextFormatting.GRAY + I18n.format("ftbquests.task.click_to_submit"));
		}
	}

	@SideOnly(Side.CLIENT)
	public void onButtonClicked()
	{
		new MessageSubmitTask(id).sendToServer();
	}

	public boolean submitItemsOnInventoryChange()
	{
		return false;
	}

	@Nullable
	@SideOnly(Side.CLIENT)
	public Object getJEIFocus()
	{
		return null;
	}
}