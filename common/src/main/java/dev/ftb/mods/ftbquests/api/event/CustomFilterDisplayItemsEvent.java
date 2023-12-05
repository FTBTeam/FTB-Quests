package dev.ftb.mods.ftbquests.api.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.function.Consumer;

public class CustomFilterDisplayItemsEvent {
    private final Consumer<ItemStack> callback;

    public static final Event<Consumer<CustomFilterDisplayItemsEvent>> ADD_ITEMSTACK = EventFactory.createConsumerLoop();

    public CustomFilterDisplayItemsEvent(Consumer<ItemStack> callback) {
        this.callback = callback;
    }

    public void add(ItemStack stack) {
        callback.accept(stack);
    }

    public void add(Collection<ItemStack> stacks) {
        stacks.forEach(callback::accept);
    }
}
