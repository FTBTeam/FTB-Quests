package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import dev.latvian.kubejs.server.ServerJS;
import net.minecraft.entity.player.EntityPlayerMP;

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
	public void check(CustomTask.Data taskData, EntityPlayerMP player)
	{
		CustomTaskDataWrapper wrapper = new CustomTaskDataWrapper(taskData);
		long prev = wrapper.progress;
		checkerJS.check(wrapper, ServerJS.instance.player(player.getUniqueID()));

		if (prev != wrapper.progress)
		{
			taskData.setProgress(wrapper.progress);
		}
	}
}