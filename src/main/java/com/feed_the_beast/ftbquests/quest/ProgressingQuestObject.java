package com.feed_the_beast.ftbquests.quest;

import net.minecraft.util.text.TextFormatting;

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

	public final boolean isComplete(IProgressData data)
	{
		return getProgress(data) >= getMaxProgress();
	}

	public final String getCompletionSuffix(IProgressData data)
	{
		int max = getMaxProgress();

		if (max == 0)
		{
			return TextFormatting.DARK_GRAY + " 0%";
		}

		int prog = getProgress(data);
		return TextFormatting.DARK_GRAY + " " + (int) (Math.min(prog, max) * 100D / (double) max) + "%";
	}
}