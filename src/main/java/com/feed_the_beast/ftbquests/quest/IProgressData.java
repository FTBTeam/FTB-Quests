package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;

/**
 * @author LatvianModder
 */
public interface IProgressData
{
	String getTeamID();

	QuestTaskData getQuestTaskData(QuestTask task);

	void syncTask(QuestTaskData data);

	void removeTask(QuestTask task);

	void createTaskData(QuestTask task);
}