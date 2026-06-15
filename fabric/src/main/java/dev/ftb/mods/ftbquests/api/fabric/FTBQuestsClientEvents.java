package dev.ftb.mods.ftbquests.api.fabric;

import dev.ftb.mods.ftbquests.api.event.CustomFilterDisplayItemsEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class FTBQuestsClientEvents {
    public static final Event<CustomFilterDisplayItemsEvent> CUSTOM_FILTER_DISPLAY_ITEMS
            = EventFactory.createArrayBacked(CustomFilterDisplayItemsEvent.class,
            callbacks -> data -> {
                for (var c : callbacks) {
                    c.accept(data);
                }
            });
}
