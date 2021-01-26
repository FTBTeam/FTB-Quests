package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.events.CustomRewardEvent;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.server.level.ServerPlayer;

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
		return FTBQuestsRewards.CUSTOM.get();
	}

	@Override
	public void claim(ServerPlayer player, boolean notify)
	{
		CustomRewardEvent.EVENT.invoker().act(new CustomRewardEvent(this, player, notify));
	}
}