package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import it.unimi.dsi.fastutil.ints.IntCollection;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Collection;
import java.util.UUID;

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

	IntCollection getClaimedRewards(UUID player);

	boolean isRewardClaimed(UUID player, QuestReward reward);

	void claimReward(EntityPlayer player, QuestReward reward);

	void unclaimRewards(Collection<QuestReward> rewards);
}