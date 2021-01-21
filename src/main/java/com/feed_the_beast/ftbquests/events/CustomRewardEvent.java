package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.reward.CustomReward;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * @author LatvianModder
 */
@Cancelable
public class CustomRewardEvent extends FTBQuestsEvent
{
	private final CustomReward reward;
	private final ServerPlayer player;
	private final boolean notify;

	public CustomRewardEvent(CustomReward r, ServerPlayer p, boolean n)
	{
		reward = r;
		player = p;
		notify = n;
	}

	public CustomReward getReward()
	{
		return reward;
	}

	public ServerPlayer getPlayer()
	{
		return player;
	}

	public boolean getNotify()
	{
		return notify;
	}
}