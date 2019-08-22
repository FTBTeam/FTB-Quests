package com.feed_the_beast.ftbquests.integration.kubejs;

import dev.latvian.kubejs.player.PlayerEventJS;
import net.minecraft.entity.Entity;

/**
 * @author LatvianModder
 */
public class CustomRewardEventJS extends PlayerEventJS
{
	public CustomRewardEventJS(Entity p)
	{
		super(p);
	}

	@Override
	public boolean canCancel()
	{
		return true;
	}
}