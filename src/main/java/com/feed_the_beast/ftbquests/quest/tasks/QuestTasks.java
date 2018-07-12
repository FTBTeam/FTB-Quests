package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
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
		@Nullable
		QuestTask create(Quest quest, int id, NBTTagCompound nbt);
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

	public static QuestTask createTask(Quest quest, int id, NBTTagCompound nbt)
	{
		TaskProvider provider = MAP0.get(nbt.getString("type"));

		if (provider != null)
		{
			QuestTask task = provider.create(quest, id, nbt);

			if (task != null && !task.isInvalid())
			{
				return task;
			}
		}

		return new UnknownTask(quest, id, nbt);
	}
}