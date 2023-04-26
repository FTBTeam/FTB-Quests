package dev.ftb.mods.ftbquests.util;

import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.InventoryMenu;
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

		file.withPlayerContext(player, () -> {
			for (Task task : file.getSubmitTasks()) {
				if (task.id != sourceTask && data.canStartTasks(task.quest)) {
					task.submitTask(data, player, craftedItem);
				}
			}
		});
	}

	@Override
	public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int j) {
	}

	@Override
	public void slotChanged(AbstractContainerMenu container, int index, ItemStack stack) {
		if (!stack.isEmpty() && container.getSlot(index).container == player.getInventory()
			&& index >= InventoryMenu.INV_SLOT_START && index < InventoryMenu.USE_ROW_SLOT_END) {
			// Only checking for items in the main inventory & hotbar
			// Armor slots can contain items with rapidly changing NBT (especially powered modded armor)
			//  which can trigger a lot of unnecessary inventory scans
			DeferredInventoryDetection.scheduleInventoryCheck(player);
		}
	}
}