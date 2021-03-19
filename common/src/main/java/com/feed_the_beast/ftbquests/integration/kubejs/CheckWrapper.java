package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import dev.latvian.kubejs.server.ServerJS;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class CheckWrapper implements CustomTask.Check {
	private final CustomTaskCheckerJS checkerJS;

	CheckWrapper(CustomTaskCheckerJS c) {
		checkerJS = c;
	}

	@Override
	public void check(CustomTask.Data taskData, ServerPlayer player) {
		checkerJS.check(taskData, ServerJS.instance.getPlayer(player));
	}
}