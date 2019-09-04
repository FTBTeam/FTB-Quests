package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.events.CustomRewardEvent;
import com.feed_the_beast.ftbquests.quest.reward.CustomReward;
import dev.latvian.kubejs.documentation.DocClass;
import dev.latvian.kubejs.documentation.DocMethod;
import dev.latvian.kubejs.entity.EntityJS;
import dev.latvian.kubejs.player.PlayerEventJS;

/**
 * @author LatvianModder
 */
@DocClass
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

	@DocMethod
	public CustomReward getReward()
	{
		return event.getReward();
	}

	@DocMethod
	public boolean getNotify()
	{
		return event.getNotify();
	}
}