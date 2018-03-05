package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;

/**
 * @author LatvianModder
 */
public interface IProgressData
{
	int getQuestTaskProgress(QuestTask task);
}