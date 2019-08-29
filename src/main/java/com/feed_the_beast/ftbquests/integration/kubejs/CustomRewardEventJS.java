package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.quest.reward.CustomReward;
import dev.latvian.kubejs.player.PlayerEventJS;
import net.minecraft.entity.Entity;

/**
 * @author LatvianModder
 */
public class CustomRewardEventJS extends PlayerEventJS
{
	public final CustomReward reward;
	public final boolean notify;

	public CustomRewardEventJS(Entity p, CustomReward r, boolean n)
	{
		super(p);
		reward = r;
		notify = n;
	}

	@Override
	public boolean canCancel()
	{
		return true;
	}
}