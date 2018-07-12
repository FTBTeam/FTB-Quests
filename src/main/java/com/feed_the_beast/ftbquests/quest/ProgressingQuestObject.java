package com.feed_the_beast.ftbquests.quest;

/**
 * @author LatvianModder
 */
public abstract class ProgressingQuestObject extends QuestObject
{
	public ProgressingQuestObject(int i)
	{
		super(i);
	}

	public abstract int getProgress(IProgressData data);

	public abstract int getMaxProgress();

	public abstract void resetProgress(IProgressData data);

	public double getRelativeProgress(IProgressData data)
	{
		int max = getMaxProgress();

		if (max == 0)
		{
			return 0D;
		}

		int progress = getProgress(data);

		if (progress >= max)
		{
			return 1D;
		}

		return (double) progress / (double) max;
	}

	public final boolean isComplete(IProgressData data)
	{
		int max = getMaxProgress();
		return max > 0 && getProgress(data) >= max;
	}
}