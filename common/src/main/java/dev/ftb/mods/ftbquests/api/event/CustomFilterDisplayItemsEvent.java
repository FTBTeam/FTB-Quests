package dev.ftb.mods.ftbquests.api.event;

import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.function.Consumer;

/// This event is fired on the client to allow mods to register custom items to be displayed when filters (such as the
/// Smart Filter from FTB Filter System) look for matching items for their filter. By default, only items in the creative
/// search tab are checked for, so this event is useful for adding items with custom data components.
///
/// KubeJS integration for this event is provided via FTB XMod Compat, and is the typical way for modpack makers to
/// add custom display items for any filters they create. An example KubeJS snippet (to be placed in
/// kubejs/client_scripts) to register an item:
/// <pre>
///   FTBQuestsEvents.customFilterItem(event => {
///      event.add('minecraft:iron_axe {display:{Name:{text:\"Test Axe!\"}}, Damage: 50}')
///      event.add('minecraft:diamond_axe {display:{Name:{text:\"Test Axe 2!\"}}, Damage: 300}')
///   })
/// </pre>
public interface CustomFilterDisplayItemsEvent extends Consumer<ItemStack> {
    record Data(Consumer<ItemStack> consumer) {
        public void add(ItemStack stack) {
            consumer.accept(stack);
        }

        public void add(Collection<ItemStack> stacks) {
            stacks.forEach(consumer);
        }
    }
}
