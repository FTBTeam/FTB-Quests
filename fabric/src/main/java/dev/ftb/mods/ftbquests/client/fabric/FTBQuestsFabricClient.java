package dev.ftb.mods.ftbquests.client.fabric;

import dev.ftb.mods.ftblibrary.fabric.FTBLibraryFabricEvents;
import dev.ftb.mods.ftbquests.api.fabric.FTBQuestsEvents;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.FTBQuestsClientEventHandler;
import dev.ftb.mods.ftbquests.client.TaskScreenRenderer;
import dev.ftb.mods.ftbquests.registry.ModBlockEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class FTBQuestsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FTBQuestsClient client = new FTBQuestsClient();
        FTBQuestsClientEventHandler eventHandler = client.eventHandler;

        FTBLibraryFabricEvents.SIDEBAR_BUTTON_CREATED.register(eventHandler::onSidebarButtonCreated);
        FTBLibraryFabricEvents.CUSTOM_CLICK.register(eventHandler::onCustomClick);

        ClientLifecycleEvents.CLIENT_STARTED.register(eventHandler::onClientSetup);
        ClientTickEvents.START_CLIENT_TICK.register(eventHandler::onKeyEvent);
        ClientTickEvents.END_CLIENT_TICK.register(eventHandler::onClientTick);
        ClientPlayConnectionEvents.JOIN.register((_, _, _) -> eventHandler.onPlayerLogin());
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> eventHandler.onPlayerLogout());

        FTBQuestsEvents.CLEAR_FILE_CACHE.register(eventHandler::onFileCacheClear);

        BlockEntityRenderers.register(ModBlockEntityTypes.CORE_TASK_SCREEN.get(), TaskScreenRenderer::new);

        // See TextureAtlasMixin for other "event" handlers

        HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, FTBQuestsClient.GUI_OVERLAY_ID, eventHandler::renderGuiOverlay);
    }
}
