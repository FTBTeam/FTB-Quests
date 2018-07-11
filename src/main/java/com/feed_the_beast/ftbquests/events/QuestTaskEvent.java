package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
@Cancelable
public class QuestTaskEvent extends FTBQuestsEvent
{
	private final Quest quest;
	private final int id;
	private final NBTTagCompound nbt;
	private QuestTask task = null;

	public QuestTaskEvent(Quest q, int i, NBTTagCompound n)
	{
		quest = q;
		id = i;
		nbt = n;
	}

	public Quest getQuest()
	{
		return quest;
	}

	public int getID()
	{
		return id;
	}

	public NBTTagCompound getData()
	{
		return nbt;
	}

	@Nullable
	public QuestTask getTask()
	{
		return task;
	}

	public void setTask(QuestTask t)
	{
		task = t;
	}
}