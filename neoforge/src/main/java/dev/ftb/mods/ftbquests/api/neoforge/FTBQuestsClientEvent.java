package dev.ftb.mods.ftbquests.api.neoforge;

import dev.ftb.mods.ftblibrary.api.neoforge.BaseEventWithData;
import dev.ftb.mods.ftbquests.api.event.CustomFilterDisplayItemsEvent;

public class FTBQuestsClientEvent {
    public static class CustomFilterDisplayItems extends BaseEventWithData<CustomFilterDisplayItemsEvent.Data> {
        public CustomFilterDisplayItems(CustomFilterDisplayItemsEvent.Data data) {
            super(data);
        }
    }
}
