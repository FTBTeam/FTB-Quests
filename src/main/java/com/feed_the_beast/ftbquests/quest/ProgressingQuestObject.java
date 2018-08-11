package com.feed_the_beast.ftbquests.quest;

/**
 * @author LatvianModder
 */
public abstract class ProgressingQuestObject extends QuestObject
{
	public abstract long getProgress(IProgressData data);

	public abstract long getMaxProgress();

	public abstract void resetProgress(IProgressData data);

	public double getRelativeProgress(IProgressData data)
	{
		long max = getMaxProgress();

		if (max == 0)
		{
			return 0D;
		}

		long progress = getProgress(data);

		if (progress >= max)
		{
			return 1D;
		}

		return (double) progress / (double) max;
	}

	public final boolean isComplete(IProgressData data)
	{
		long max = getMaxProgress();
		return max > 0 && getProgress(data) >= max;
	}
}