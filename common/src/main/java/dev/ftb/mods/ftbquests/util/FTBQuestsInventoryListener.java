package dev.ftb.mods.ftbquests.util;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import me.shedaniel.architectury.hooks.PlayerHooks;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class FTBQuestsInventoryListener implements ContainerListener {
	public final ServerPlayer player;

	public FTBQuestsInventoryListener(ServerPlayer p) {
		player = p;
	}

	public static void detect(ServerPlayer player, ItemStack craftedItem, long sourceTask) {
		ServerQuestFile file = ServerQuestFile.INSTANCE;

		if (file == null || PlayerHooks.isFake(player)) {
			return;
		}

		TeamData data = file.getNullableTeamData(FTBTeamsAPI.getPlayerTeamID(player.getUUID()));

		if (data == null || data.isLocked()) {
			return;
		}

		file.currentPlayer = player;

		for (Task task : file.getSubmitTasks()) {
			if (task.id != sourceTask && data.canStartTasks(task.quest)) {
				task.submitTask(data, player, craftedItem);
			}
		}

		file.currentPlayer = null;
	}

	@Override
	public void refreshContainer(AbstractContainerMenu container, NonNullList<ItemStack> itemsList) {
		detect(player, ItemStack.EMPTY, 0);
	}

	@Override
	public void slotChanged(AbstractContainerMenu container, int index, ItemStack stack) {
		if (!stack.isEmpty() && container.getSlot(index).container == player.inventory) {
			detect(player, ItemStack.EMPTY, 0);
		}
	}

	@Override
	public void setContainerData(AbstractContainerMenu container, int id, int value) {
	}
}