package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;

import java.util.Collection;

/**
 * @author LatvianModder
 */
public interface ITeamData
{
	String getTeamID();

	QuestTaskData getQuestTaskData(QuestTask task);

	void syncTask(QuestTaskData data);

	void removeTask(QuestTask task);

	void createTaskData(QuestTask task);

	void unclaimRewards(Collection<QuestReward> rewards);

	long getVariable(QuestVariable variable);

	void setVariable(QuestVariable variable, long value);
}