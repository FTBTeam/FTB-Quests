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

	public CustomRewardEvent(CustomReward r, EntityPlayerMP p)
	{
		reward = r;
		player = p;
	}

	public CustomReward getReward()
	{
		return reward;
	}

	public EntityPlayerMP getPlayer()
	{
		return player;
	}
}