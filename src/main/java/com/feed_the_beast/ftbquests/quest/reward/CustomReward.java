package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.events.CustomRewardEvent;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;

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
	public void claim(ServerPlayerEntity player, boolean notify)
	{
		MinecraftForge.EVENT_BUS.post(new CustomRewardEvent(this, player, notify));
	}
}