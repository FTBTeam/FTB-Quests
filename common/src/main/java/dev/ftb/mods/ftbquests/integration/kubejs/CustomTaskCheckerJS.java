package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.ftb.mods.ftbquests.quest.task.CustomTask;
import dev.latvian.mods.kubejs.player.PlayerJS;

/**
 * @author LatvianModder
 */
@FunctionalInterface
public interface CustomTaskCheckerJS {
	void check(CustomTask.Data taskData, PlayerJS player);
}