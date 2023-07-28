package dev.ftb.mods.ftbquests.forge;

import dev.ftb.mods.ftbquests.client.FTBQuestsClientEventHandler;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

public class ClientSetup {
    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(ClientSetup::onTextureStitch);
    }

    private static void onTextureStitch(TextureStitchEvent.Post event) {
        FTBQuestsClientEventHandler.onTextureStitchPost(event.getAtlas());
    }
}
