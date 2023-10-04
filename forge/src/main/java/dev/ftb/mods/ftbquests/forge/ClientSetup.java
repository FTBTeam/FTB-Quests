package dev.ftb.mods.ftbquests.forge;

import dev.ftb.mods.ftbquests.client.FTBQuestsClientEventHandler;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientSetup {
    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::onTextureStitch);
    }

    private static void onTextureStitch(TextureStitchEvent.Post event) {
        FTBQuestsClientEventHandler.onTextureStitchPost(event.getAtlas());
    }
}
