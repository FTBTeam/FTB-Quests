package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.events.CustomRewardEvent;
import com.feed_the_beast.ftbquests.quest.reward.CustomReward;
import dev.latvian.kubejs.entity.EntityJS;
import dev.latvian.kubejs.player.PlayerEventJS;

/**
 * @author LatvianModder
 */
public class CustomRewardEventJS extends PlayerEventJS
{
	public final transient CustomRewardEvent event;

	public CustomRewardEventJS(CustomRewardEvent e)
	{
		event = e;
	}

	@Override
	public boolean canCancel()
	{
		return true;
	}

	@Override
	public EntityJS getEntity()
	{
		return entityOf(event.getPlayer());
	}

	public CustomReward getReward()
	{
		return event.getReward();
	}

	public boolean getNotify()
	{
		return event.getNotify();
	}
}