package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftbquests.quest.IProgressData;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagLong;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public abstract class SimpleQuestTaskData<T extends QuestTask> extends QuestTaskData<T>
{
	public long progress = 0L;

	public SimpleQuestTaskData(T q, IProgressData d)
	{
		super(q, d);
	}

	@Nullable
	@Override
	public NBTBase toNBT()
	{
		return progress <= 0L ? null : new NBTTagLong(progress);
	}

	@Override
	public void fromNBT(@Nullable NBTBase nbt)
	{
		progress = nbt instanceof NBTPrimitive ? ((NBTPrimitive) nbt).getLong() : 0L;
	}

	@Override
	public long getProgress()
	{
		return progress;
	}

	@Override
	public void resetProgress()
	{
		progress = 0L;
	}
}