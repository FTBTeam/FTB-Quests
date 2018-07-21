package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftbquests.block.TileQuest;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestList;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public abstract class QuestTask extends ProgressingQuestObject implements IStringSerializable
{
	public final Quest quest;

	public QuestTask(Quest q, NBTTagCompound nbt)
	{
		super(q.chapter.list.getID(nbt));
		quest = q;
	}

	public abstract QuestTaskData createData(IProgressData data);

	@Override
	public final QuestList getQuestList()
	{
		return quest.getQuestList();
	}

	@Override
	public final QuestObjectType getObjectType()
	{
		return QuestObjectType.TASK;
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

	@Nullable
	public TileQuest createCustomTileEntity(World world)
	{
		return null;
	}
}