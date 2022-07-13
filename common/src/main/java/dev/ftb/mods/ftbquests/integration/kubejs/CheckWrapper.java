package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.ftb.mods.ftbquests.quest.task.CustomTask;
import dev.latvian.mods.kubejs.core.PlayerSelector;
import dev.latvian.mods.kubejs.server.ServerJS;
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
		checkerJS.check(taskData, ServerJS.instance.getPlayer(PlayerSelector.mc(player)));
	}
}
