package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftbquests.quest.IProgressData;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public abstract class SimpleQuestTaskData<T extends QuestTask> extends QuestTaskData<T>
{
	@Nullable
	public static NBTBase longToNBT(long value)
	{
		if (value <= 0L)
		{
			return null;
		}
		else if (value <= Byte.MAX_VALUE)
		{
			return new NBTTagByte((byte) value);
		}
		else if (value <= Short.MAX_VALUE)
		{
			return new NBTTagShort((short) value);
		}
		else if (value <= Integer.MAX_VALUE)
		{
			return new NBTTagInt((int) value);
		}

		return new NBTTagLong(value);
	}

	public long progress = 0L;

	public SimpleQuestTaskData(T q, IProgressData d)
	{
		super(q, d);
	}

	@Nullable
	@Override
	public NBTBase toNBT()
	{
		return progress <= 0L ? null : longToNBT(progress);
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

	@Override
	public void completeInstantly()
	{
		progress = task.getMaxProgress();
	}
}