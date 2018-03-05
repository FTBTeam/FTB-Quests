package com.feed_the_beast.ftbquests.quest;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public final class QuestDependency implements IProgressing
{
	public final Quest quest;
	public final QuestChapter chapter;

	public QuestDependency(@Nullable QuestChapter c, @Nullable Quest q)
	{
		if (c == null && q == null)
		{
			throw new IllegalArgumentException("Both chapter and quest can't be null!");
		}

		quest = q;
		chapter = c == null ? quest.chapter : c;
	}

	@Override
	public int getProgress(IProgressData data)
	{
		return quest == null ? chapter.getProgress(data) : quest.getProgress(data);
	}

	@Override
	public int getMaxProgress()
	{
		return quest == null ? chapter.getMaxProgress() : quest.getMaxProgress();
	}

	public String toString()
	{
		return quest == null ? chapter.getName() + ":*" : quest.id.toString();
	}
}