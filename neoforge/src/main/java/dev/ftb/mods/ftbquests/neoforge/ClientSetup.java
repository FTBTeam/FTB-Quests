package dev.ftb.mods.ftbquests.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;

import dev.ftb.mods.ftbquests.client.FTBQuestsClientEventHandler;
import dev.ftb.mods.ftbquests.client.neoforge.ModelBakeEventHandler;

public class ClientSetup {
    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::onTextureStitch);
        modEventBus.addListener(ModelBakeEventHandler::onModelBake);
    }

    private static void onTextureStitch(TextureAtlasStitchedEvent event) {
        FTBQuestsClientEventHandler.onTextureStitchPost(event.getAtlas());
    }
}
