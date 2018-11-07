package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.net.MessageSubmitItems;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.tile.TileScreenCore;
import com.feed_the_beast.ftbquests.tile.TileScreenPart;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
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
	public abstract void writeData(NBTTagCompound nbt);

	@Override
	public abstract void readData(NBTTagCompound nbt);

	public abstract QuestTaskData createData(ITeamData data);

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
	public final QuestObjectType getObjectType()
	{
		return QuestObjectType.TASK;
	}

	@Override
	public final String getID()
	{
		return quest.chapter.id + ':' + quest.id + ':' + id;
	}

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
	public void onCompleted(ITeamData data)
	{
		super.onCompleted(data);
		new ObjectCompletedEvent.TaskEvent(data, this).post();

		if (quest.isComplete(data))
		{
			quest.onCompleted(data);
		}
	}

	@Override
	public long getMaxProgress()
	{
		return 1;
	}

	public String getMaxProgressString()
	{
		return Long.toString(getMaxProgress());
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
	public void deleteSelf()
	{
		quest.tasks.remove(this);

		for (ITeamData data : quest.chapter.file.getAllData())
		{
			data.removeTask(this);
		}

		super.deleteSelf();
	}

	@Override
	public void deleteChildren()
	{
	}

	@Override
	public void onCreated()
	{
		quest.tasks.add(this);

		for (ITeamData data : quest.chapter.file.getAllData())
		{
			data.createTaskData(this);
		}
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return QuestTaskType.getType(getClass()).getDisplayName();
	}

	public Class<? extends TileScreenCore> getScreenCoreClass()
	{
		return TileScreenCore.class;
	}

	public Class<? extends TileScreenPart> getScreenPartClass()
	{
		return TileScreenPart.class;
	}

	public TileScreenCore createScreenCore(World world)
	{
		return new TileScreenCore();
	}

	public TileScreenPart createScreenPart(World world)
	{
		return new TileScreenPart();
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

	@Override
	public Icon getAltIcon()
	{
		return GuiIcons.ACCEPT;
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
		if (canInsertItem())
		{
			new MessageSubmitItems(uid).sendToServer();
		}
	}

	public boolean submitItemsOnInventoryChange()
	{
		return false;
	}
}