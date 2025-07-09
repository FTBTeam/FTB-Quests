package dev.ftb.mods.ftbquests.util;

import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class FTBQuestsInventoryListener implements ContainerListener {
	public final ServerPlayer player;

	private static final Map<Item, List<ItemStack>> inventorySummaryCache = new HashMap<>();
	private static final List<ItemStack> nonEmptyStacks = new ArrayList<>();

	public FTBQuestsInventoryListener(ServerPlayer p) {
		player = p;
	}

	public static void detect(ServerPlayer player, ItemStack craftedItem, long sourceTask) {
		ServerQuestFile file = ServerQuestFile.INSTANCE;

		if (file == null || PlayerHooks.isFake(player)) {
			return;
		}

		List<Task> tasksToCheck = craftedItem.isEmpty() ? file.getSubmitTasks() : file.getCraftingTasks();

		if (!tasksToCheck.isEmpty()) {
			FTBTeamsAPI.api().getManager().getTeamForPlayer(player).ifPresent(team -> {
				TeamData data = file.getNullableTeamData(team.getId());
				if (data != null && !data.isLocked()) {
					file.withPlayerContext(player, () -> {
						buildInventorySummary(player);
						for (Task task : tasksToCheck) {
							if (task.id != sourceTask && data.canStartTasks(task.getQuest())) {
								task.submitTask(data, player, craftedItem);
							}
						}
						inventorySummaryCache.clear();
						nonEmptyStacks.clear();
					});
				}
			});
		}
	}

	private static void buildInventorySummary(ServerPlayer player) {
		player.getInventory().items.forEach(stack -> {
			if (!stack.isEmpty()) {
				inventorySummaryCache.computeIfAbsent(stack.getItem(), k -> new ArrayList<>()).add(stack);
				nonEmptyStacks.add(stack);
			}
		});
	}

	public static Collection<ItemStack> getStacksForPlayerOfType(Item item) {
		return inventorySummaryCache.getOrDefault(item, List.of());
	}

	public static Collection<ItemStack> getAllNonEmptyStacksForPlayer() {
		return nonEmptyStacks;
	}

	@Override
	public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int j) {
	}

	@Override
	public void slotChanged(AbstractContainerMenu menu, int index, ItemStack stack) {
		if (!stack.isEmpty() && menu.getSlot(index).container == player.getInventory()) {
			int slotNum = menu.getSlot(index).getContainerSlot();
			if (slotNum >= 0 && slotNum < player.getInventory().items.size()) {
				// Only checking for items in the main inventory & hotbar
				// Armor slots can contain items with rapidly changing NBT (especially powered modded armor)
				//  which can trigger a lot of unnecessary inventory scans
				int delay = Mth.clamp(ServerQuestFile.INSTANCE.getDetectionDelay(), 0, 200);
				if (delay == 0) {
					FTBQuestsInventoryListener.detect(player, ItemStack.EMPTY, 0);
				} else {
					DeferredInventoryDetection.scheduleInventoryCheck(player, delay);
				}
			}
		}
	}
}