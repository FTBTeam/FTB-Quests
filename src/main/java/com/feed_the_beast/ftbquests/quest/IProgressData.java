package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskKey;

/**
 * @author LatvianModder
 */
public interface IProgressData
{
	int getQuestTaskProgress(QuestTaskKey task);

	boolean setQuestTaskProgress(QuestTaskKey task, int progress);
}