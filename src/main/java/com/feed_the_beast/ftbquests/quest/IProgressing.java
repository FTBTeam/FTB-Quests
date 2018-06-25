package com.feed_the_beast.ftbquests.quest;

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
		return getCompletionString(getProgress(data), getMaxProgress());
	}

	static String getCompletionString(int prog, int maxProg)
	{
		if (maxProg > 0)
		{
			if (prog > maxProg)
			{
				prog = maxProg;
			}

			return String.format("%d/%d [%d%%]", prog, maxProg, (int) (prog * 100D / (double) maxProg));
		}

		return "0/0 [0%]";
	}
}