package dev.ftb.mods.ftbquests.util;

import dev.ftb.mods.ftbquests.integration.item_filtering.ItemMatchingSystem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * A highly-transient summary of items in a player's inventory, organised to make it efficient for
 * {@link dev.ftb.mods.ftbquests.quest.task.ItemTask} to do player inventory searches. Built before tasks are
 * scanned on player login, and on player inventory change.
 */
public class PlayerInventorySummary {
    private static final List<ItemStack> nonEmptyStacks = new ArrayList<>();
    private static final Map<Item, List<ItemStack>> stacksByItem = new HashMap<>();

    public static void build(ServerPlayer player) {
        nonEmptyStacks.clear();
        stacksByItem.clear();

        player.getInventory().forEach(stack -> {
            if (!stack.isEmpty()) {
                nonEmptyStacks.add(stack);
                stacksByItem.computeIfAbsent(stack.getItem(), k -> new ArrayList<>()).add(stack);
            }
        });
    }

    public static Collection<ItemStack> getRelevantItems(ItemStack stack) {
        return ItemMatchingSystem.INSTANCE.isItemFilter(stack) ?
                nonEmptyStacks :
                stacksByItem.getOrDefault(stack.getItem(), List.of());
    }
}
