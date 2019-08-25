package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.reward.CustomReward;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * @author LatvianModder
 */
@Cancelable
public class CustomRewardEvent extends FTBQuestsEvent
{
	private final CustomReward reward;
	private final EntityPlayerMP player;
	private final boolean notify;

	public CustomRewardEvent(CustomReward r, EntityPlayerMP p, boolean n)
	{
		reward = r;
		player = p;
		notify = n;
	}

	public CustomReward getReward()
	{
		return reward;
	}

	public EntityPlayerMP getPlayer()
	{
		return player;
	}

	public boolean getNotify()
	{
		return notify;
	}
}