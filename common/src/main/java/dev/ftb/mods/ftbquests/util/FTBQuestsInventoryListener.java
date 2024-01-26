package dev.ftb.mods.ftbquests.util;

import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

import java.util.List;

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

		List<Task> tasksToCheck = craftedItem.isEmpty() ? file.getSubmitTasks() : file.getCraftingTasks();

		if (!tasksToCheck.isEmpty()) {
			file.withPlayerContext(player, () -> {
				for (Task task : tasksToCheck) {
					if (task.id != sourceTask && data.canStartTasks(task.quest)) {
						task.submitTask(data, player, craftedItem);
					}
				}
			});
		}
	}

	@Override
	public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int j) {
	}

	@Override
	public void slotChanged(AbstractContainerMenu container, int index, ItemStack stack) {
		if (!stack.isEmpty() && container.getSlot(index).container == player.getInventory()) {
			int slotNum = container.getSlot(index).getContainerSlot();
			if (slotNum >= 0 && slotNum < player.getInventory().items.size()) {
				// Only checking for items in the main inventory & hotbar
				// Armor slots can contain items with rapidly changing NBT (especially powered modded armor)
				//  which can trigger a lot of unnecessary inventory scans
				int delay = Mth.clamp(ServerQuestFile.INSTANCE.detectionDelay, 0, 200);
				if (delay == 0) {
					FTBQuestsInventoryListener.detect(player, ItemStack.EMPTY, 0);
				} else {
					DeferredInventoryDetection.scheduleInventoryCheck(player, delay);
				}
			}
		}
	}
}