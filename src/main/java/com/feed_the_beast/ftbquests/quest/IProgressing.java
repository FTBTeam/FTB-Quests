package com.feed_the_beast.ftbquests.quest;

import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public interface IProgressing
{
	int getProgress(IProgressData data);

	int getMaxProgress();

	default boolean isComplete(IProgressData data)
	{
		return getProgress(data) >= getMaxProgress();
	}

	default String getCompletionString(IProgressData data)
	{
		int maxProg = getMaxProgress();
		int prog = Math.max(maxProg, getProgress(data));

		if (prog <= 0 || maxProg <= 0)
		{
			return TextFormatting.DARK_GRAY + "0/0 [0%]";
		}

		return TextFormatting.DARK_GRAY.toString() + prog + "/" + maxProg + " [" + (int) (prog * 100D / (double) maxProg) + "%]";
	}
}