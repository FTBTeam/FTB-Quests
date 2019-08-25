package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.events.CustomRewardEvent;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class CustomReward extends Reward
{
	public CustomReward(Quest quest)
	{
		super(quest);
	}

	@Override
	public RewardType getType()
	{
		return FTBQuestsRewards.CUSTOM;
	}

	@Override
	public void claim(EntityPlayerMP player, boolean notify)
	{
		new CustomRewardEvent(this, player, notify).post();
	}
}