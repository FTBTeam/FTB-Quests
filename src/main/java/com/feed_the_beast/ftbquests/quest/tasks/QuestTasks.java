package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class QuestTasks
{
	public interface TaskProvider
	{
		QuestTask create(Quest quest, NBTTagCompound nbt);
	}

	private static final Map<String, TaskProvider> MAP0 = new HashMap<>();
	public static final Map<String, TaskProvider> MAP = Collections.unmodifiableMap(MAP0);

	public static void add(String type, TaskProvider provider)
	{
		MAP0.put(type, provider);
	}

	public static void init()
	{
		add(UnknownTask.ID, UnknownTask::new);
		add(ItemTask.ID, ItemTask::new);
		add(FluidTask.ID, FluidTask::new);
		add(ForgeEnergyTask.ID, ForgeEnergyTask::new);
	}

	public static QuestTask createTask(Quest quest, NBTTagCompound nbt)
	{
		TaskProvider provider = MAP0.get(nbt.getString("type"));

		QuestTask task;

		if (provider != null)
		{
			task = provider.create(quest, nbt);
		}
		else
		{
			task = new UnknownTask(quest, nbt);
		}

		task.readID(nbt);
		return task;
	}
}