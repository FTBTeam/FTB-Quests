package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskData;
import net.minecraft.util.text.ITextComponent;

import java.util.Collection;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public interface ITeamData
{
	short getTeamUID();

	String getTeamID();

	ITextComponent getDisplayName();

	QuestFile getFile();

	QuestTaskData getQuestTaskData(QuestTask task);

	void syncTask(QuestTaskData data);

	void removeTask(QuestTask task);

	void createTaskData(QuestTask task);

	void unclaimRewards(Collection<QuestReward> rewards);

	long getVariable(int variable);

	void setVariable(int variable, long value);

	boolean isRewardClaimed(UUID player, QuestReward reward);
}