package dev.ftb.mods.ftbquests.client.neoforge;

import dev.ftb.mods.ftblibrary.api.neoforge.FTBLibraryEvent;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.api.neoforge.FTBQuestsEvent;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.FTBQuestsClientEventHandler;
import dev.ftb.mods.ftbquests.registry.ModBlockEntityTypes;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.lifecycle.ClientLifecycleEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = FTBQuestsAPI.MOD_ID, dist = Dist.CLIENT)
public class FTBQuestsNeoforgeClient {
    private final FTBQuestsClient client;

    public FTBQuestsNeoforgeClient(IEventBus modBus) {
        client = new FTBQuestsClient();

        IEventBus bus = NeoForge.EVENT_BUS;

        bus.addListener(FTBLibraryEvent.SidebarButtonCreated.class, event ->
                client.eventHandler.onSidebarButtonCreated(event.getEventData()));
        bus.addListener(FTBLibraryEvent.CustomClick.class, event -> {
            if (client.eventHandler.onCustomClick(event.getEventData())) {
                event.setCanceled(true);
            }
        });

        bus.addListener(ClientStartedEvent.class, _ -> client.onClientSetup(Minecraft.getInstance()));
        bus.addListener(ClientTickEvent.Pre.class, _ -> client.eventHandler.onKeyEvent());
        bus.addListener(ClientTickEvent.Post.class, _ -> client.eventHandler.onClientTick(Minecraft.getInstance()));
        bus.addListener(ClientPlayerNetworkEvent.LoggingIn.class, event -> client.eventHandler.onPlayerLogin(event.getPlayer()));
        bus.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> client.eventHandler.onPlayerLogout(event.getPlayer()));

        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event ->
                event.registerBlockEntityRenderer(ModBlockEntityTypes.CORE_TASK_SCREEN.get(), NeoForgeTaskScreenRenderer::new));
        modBus.addListener(RegisterGuiLayersEvent.class, event ->
                event.registerAbove(VanillaGuiLayers.SUBTITLE_OVERLAY, FTBQuestsClient.GUI_OVERLAY_ID, this::renderGuiOverlay));
        modBus.addListener(this::onTextureStitch);
        modBus.addListener(ModelBakeEventHandler::onModelBake);

        bus.addListener(FTBQuestsEvent.ClearFileCache.class, event ->
                client.eventHandler.onFileCacheClear(event.getEventData().file()));
    }

    private void renderGuiOverlay(GuiGraphicsExtractor guiGraphicsExtractor, DeltaTracker deltaTracker) {
        client.eventHandler.onScreenRender(guiGraphicsExtractor, deltaTracker);
    }

    private void onTextureStitch(TextureAtlasStitchedEvent event) {
        FTBQuestsClientEventHandler.onTextureStitchPost(event.getAtlas());
    }
}
