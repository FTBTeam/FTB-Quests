package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import it.unimi.dsi.fastutil.ints.IntCollection;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author LatvianModder
 */
public interface IProgressData
{
	String getTeamID();

	QuestTaskData getQuestTaskData(int task);

	boolean claimReward(EntityPlayer player, QuestReward reward);

	IntCollection getClaimedRewards(EntityPlayer player);

	default boolean isRewardClaimed(EntityPlayer player, QuestReward reward)
	{
		return getClaimedRewards(player).contains(reward.id);
	}

	void syncTask(QuestTaskData data);
}