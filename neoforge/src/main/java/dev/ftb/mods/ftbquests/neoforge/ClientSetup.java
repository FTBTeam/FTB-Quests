package dev.ftb.mods.ftbquests.neoforge;

import dev.ftb.mods.ftbquests.client.FTBQuestsClientEventHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;

public class ClientSetup {
    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::onTextureStitch);
    }

    private static void onTextureStitch(TextureAtlasStitchedEvent event) {
        FTBQuestsClientEventHandler.onTextureStitchPost(event.getAtlas());
    }
}
