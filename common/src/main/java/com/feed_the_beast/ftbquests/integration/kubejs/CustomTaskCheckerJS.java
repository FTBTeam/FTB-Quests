package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import dev.latvian.kubejs.player.PlayerJS;

/**
 * @author LatvianModder
 */
@FunctionalInterface
public interface CustomTaskCheckerJS {
	void check(CustomTask.Data taskData, PlayerJS player);
}