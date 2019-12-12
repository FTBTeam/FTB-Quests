package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.reward.CustomReward;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * @author LatvianModder
 */
@Cancelable
public class CustomRewardEvent extends FTBQuestsEvent
{
	private final CustomReward reward;
	private final ServerPlayerEntity player;
	private final boolean notify;

	public CustomRewardEvent(CustomReward r, ServerPlayerEntity p, boolean n)
	{
		reward = r;
		player = p;
		notify = n;
	}

	public CustomReward getReward()
	{
		return reward;
	}

	public ServerPlayerEntity getPlayer()
	{
		return player;
	}

	public boolean getNotify()
	{
		return notify;
	}
}