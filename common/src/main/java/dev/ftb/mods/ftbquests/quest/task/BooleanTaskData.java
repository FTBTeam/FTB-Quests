package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftbquests.quest.PlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class BooleanTaskData<T extends Task> extends TaskData<T> {
	public BooleanTaskData(T q, PlayerData d) {
		super(q, d);
	}

	public boolean canSubmit(ServerPlayer player) {
		return true;
	}

	@Override
	public void submitTask(ServerPlayer player, ItemStack item) {
		if (!isComplete() && canSubmit(player)) {
			setProgress(1L);
		}
	}
}