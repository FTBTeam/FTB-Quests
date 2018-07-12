package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public abstract class QuestTask extends ProgressingQuestObject implements IStringSerializable
{
	public final Quest quest;

	public QuestTask(Quest q, int id)
	{
		super(id);
		quest = q;
	}

	@Override
	public abstract String getName();

	public abstract void writeData(NBTTagCompound nbt);

	public abstract Icon getIcon();

	@SideOnly(Side.CLIENT)
	public abstract String getDisplayName();

	public abstract QuestTaskData createData(IProgressData data);

	@Override
	public QuestList getQuestList()
	{
		return quest.getQuestList();
	}

	@Override
	public final int getProgress(IProgressData data)
	{
		return data.getQuestTaskData(id).getProgress();
	}

	@Override
	public int getMaxProgress()
	{
		return 1;
	}

	@Override
	public final void resetProgress(IProgressData data)
	{
		data.getQuestTaskData(id).setProgress(0, false);
	}

	@Override
	public final void delete()
	{
		super.delete();
		quest.tasks.remove(this);

		for (IProgressData data : quest.chapter.list.getAllData())
		{
			data.removeTask(id);
		}
	}
}