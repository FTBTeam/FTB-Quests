package com.feed_the_beast.ftbquests.integration.kubejs;

import dev.latvian.kubejs.player.PlayerJS;

/**
 * @author LatvianModder
 */
@FunctionalInterface
public interface CustomTaskCheckerJS
{
	void check(CustomTaskDataWrapper task, PlayerJS player);
}