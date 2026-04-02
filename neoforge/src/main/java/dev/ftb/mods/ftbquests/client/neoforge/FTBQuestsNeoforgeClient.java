package dev.ftb.mods.ftbquests.client.neoforge;

import dev.ftb.mods.ftblibrary.api.neoforge.FTBLibraryEvent;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.api.neoforge.FTBQuestsEvent;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.FTBQuestsClientEventHandler;
import dev.ftb.mods.ftbquests.registry.ModBlockEntityTypes;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = FTBQuestsAPI.MOD_ID, dist = Dist.CLIENT)
public class FTBQuestsNeoforgeClient {
    public FTBQuestsNeoforgeClient(IEventBus modBus) {
        FTBQuestsClient client = new FTBQuestsClient();
        FTBQuestsClientEventHandler eventHandler = client.eventHandler;

        IEventBus bus = NeoForge.EVENT_BUS;

        bus.addListener(FTBLibraryEvent.SidebarButtonCreated.class, event ->
                eventHandler.onSidebarButtonCreated(event.getEventData()));
        bus.addListener(FTBLibraryEvent.CustomClick.class, event -> {
            if (eventHandler.onCustomClick(event.getEventData())) {
                event.setCanceled(true);
            }
        });

        bus.addListener(ClientStartedEvent.class, _ -> eventHandler.onClientSetup(Minecraft.getInstance()));
        bus.addListener(ClientTickEvent.Pre.class, _ -> eventHandler.onKeyEvent(Minecraft.getInstance()));
        bus.addListener(ClientTickEvent.Post.class, _ -> eventHandler.onClientTick(Minecraft.getInstance()));
        bus.addListener(ClientPlayerNetworkEvent.LoggingIn.class, event -> eventHandler.onPlayerLogin());
        bus.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> eventHandler.onPlayerLogout());

        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event ->
                event.registerBlockEntityRenderer(ModBlockEntityTypes.CORE_TASK_SCREEN.get(), NeoForgeTaskScreenRenderer::new));
        modBus.addListener(RegisterGuiLayersEvent.class, event ->
                event.registerAbove(VanillaGuiLayers.SUBTITLE_OVERLAY, FTBQuestsClient.GUI_OVERLAY_ID, eventHandler::renderGuiOverlay));
        modBus.addListener(TextureAtlasStitchedEvent.class, event ->
                FTBQuestsClientEventHandler.onTextureStitchPost(event.getAtlas()));
        modBus.addListener(ModelBakeEventHandler::onModelBake);

        bus.addListener(FTBQuestsEvent.ClearFileCache.class, event ->
                eventHandler.onFileCacheClear(event.getEventData()));
    }

}
