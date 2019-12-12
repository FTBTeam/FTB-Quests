package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import dev.latvian.kubejs.server.ServerJS;
import net.minecraft.entity.player.ServerPlayerEntity;

/**
 * @author LatvianModder
 */
public class CheckWrapper implements CustomTask.Check
{
	private final CustomTaskCheckerJS checkerJS;

	CheckWrapper(CustomTaskCheckerJS c)
	{
		checkerJS = c;
	}

	@Override
	public void check(CustomTask.Data taskData, ServerPlayerEntity player)
	{
		checkerJS.check(taskData, ServerJS.instance.getPlayer(player));
	}
}