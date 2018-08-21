package com.feed_the_beast.ftbquests.quest;

/**
 * @author LatvianModder
 */
public abstract class ProgressingQuestObject extends QuestObject
{
	public abstract long getProgress(IProgressData data);

	public abstract long getMaxProgress();

	public abstract void resetProgress(IProgressData data);

	public abstract void completeInstantly(IProgressData data);

	public abstract double getRelativeProgress(IProgressData data);

	public abstract boolean isComplete(IProgressData data);
}