package dev.ftb.mods.ftbquests.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.ftb.mods.ftblibrary.api.sidebar.ButtonOverlayRender;
import dev.ftb.mods.ftblibrary.api.sidebar.SidebarButtonCreatedEvent;
import dev.ftb.mods.ftblibrary.ui.CustomClickEvent;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.events.ClearFileCacheEvent;
import dev.ftb.mods.ftbquests.item.LootCrateItem;
import dev.ftb.mods.ftbquests.net.RequestTranslationTableMessage;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.task.StructureTask;
import dev.ftb.mods.ftbquests.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static dev.ftb.mods.ftbquests.client.TaskScreenRenderer.*;

public class FTBQuestsClientEventHandler {
    private static final ResourceLocation QUESTS_BUTTON = FTBQuestsAPI.rl("quests");

    static boolean creativeTabRebuildPending = false;

    private final ObservationClientHandler observationHandler = new ObservationClientHandler();

    public static TextureAtlasSprite inputOnlySprite;
    public static TextureAtlasSprite tankSprite;
    public static TextureAtlasSprite feEnergyEmptySprite;
    public static TextureAtlasSprite feEnergyFullSprite;
    public static TextureAtlasSprite trEnergyEmptySprite;
    public static TextureAtlasSprite trEnergyFullSprite;

    public void init() {
        ClientLifecycleEvent.CLIENT_SETUP.register(this::registerItemColors);
        ClientLifecycleEvent.CLIENT_SETUP.register(this::registerBERs);
        SidebarButtonCreatedEvent.EVENT.register(this::onSidebarButtonCreated);
        ClearFileCacheEvent.EVENT.register(this::onFileCacheClear);
        ClientTickEvent.CLIENT_PRE.register(this::onKeyEvent);
        CustomClickEvent.EVENT.register(this::onCustomClick);
        ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
        ClientGuiEvent.RENDER_HUD.register(this::onScreenRender);
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(this::onPlayerLogin);
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(this::onPlayerLogout);
    }

    // Note: Architectury doesn't have a texture stitch post event anymore,
    // so this is handled by the Forge/NeoForge events, and a mixin on Fabric
    public static void onTextureStitchPost(TextureAtlas textureAtlas) {
        if (textureAtlas.location().equals(InventoryMenu.BLOCK_ATLAS)) {
            inputOnlySprite = textureAtlas.getSprite(INPUT_ONLY_TEXTURE);
            tankSprite = textureAtlas.getSprite(TANK_TEXTURE);
            feEnergyEmptySprite = textureAtlas.getSprite(FE_ENERGY_EMPTY_TEXTURE);
            feEnergyFullSprite = textureAtlas.getSprite(FE_ENERGY_FULL_TEXTURE);
            trEnergyEmptySprite = textureAtlas.getSprite(TR_ENERGY_EMPTY_TEXTURE);
            trEnergyFullSprite = textureAtlas.getSprite(TR_ENERGY_FULL_TEXTURE);
        }
    }

    private void registerBERs(Minecraft minecraft) {
        BlockEntityRendererRegistry.register(ModBlockEntityTypes.CORE_TASK_SCREEN.get(), taskScreenRenderer());
    }

    @ExpectPlatform
    public static BlockEntityRendererProvider<TaskScreenBlockEntity> taskScreenRenderer() {
        throw new AssertionError();
    }

    private void registerItemColors(Minecraft minecraft) {
        ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
            LootCrate crate = LootCrateItem.getCrate(stack, true);
            return crate == null ? 0xFFFFFFFF : (0xFF000000 | crate.getColor().rgb());
        }, ModItems.LOOTCRATE.get());
    }

    private void onSidebarButtonCreated(SidebarButtonCreatedEvent event) {
        if (event.getButton().getId().equals(QUESTS_BUTTON)) {
            event.getButton().addOverlayRender(ButtonOverlayRender.ofSimpleString(() ->
            {
                if (ClientQuestFile.exists()) {
                    if (ClientQuestFile.INSTANCE.isDisableGui() && !ClientQuestFile.INSTANCE.canEdit()) {
                        return "[X]";
                    } else if (ClientQuestFile.INSTANCE.selfTeamData.isLocked()) {
                        return "[X]";
                    } else if (ClientQuestFile.INSTANCE.selfTeamData.hasUnclaimedRewards(Minecraft.getInstance().player.getUUID(), ClientQuestFile.INSTANCE)) {
                        return "[!]";
                    }
                }

                return "";
            }));
        }
    }

    private void onFileCacheClear(BaseQuestFile file) {
        if (!file.isServerSide()) {
            observationHandler.clearCachedData();
        }
    }

    private void onKeyEvent(Minecraft mc) {
        if (ClientQuestFile.exists()
                && (!ClientQuestFile.INSTANCE.isDisableGui() || ClientQuestFile.INSTANCE.canEdit())
                && FTBQuestsKeyMappings.KEY_QUESTS.consumeClick()) {
            ClientQuestFile.openGui();
        } else if (FTBQuestsKeyMappings.KEY_CYCLE_PINNED_TRACKER.consumeClick()) {
            FTBQuestsClientConfig.cyclePinnedTrackerVisibility();
        }
    }

    private EventResult onCustomClick(CustomClickEvent event) {
        if (event.id().getNamespace().equals(FTBQuestsAPI.MOD_ID) && "open_gui".equals(event.id().getPath())) {
            // to be safe, we close the current screen before opening Quests, to avoid potential gui open-close loops with other mods
            // also save the cursor position and restore it after, since closing the screen will reset to the centre
            double mx = Minecraft.getInstance().mouseHandler.xpos();
            double my = Minecraft.getInstance().mouseHandler.ypos();
            Minecraft.getInstance().setScreen(null);
            if (ClientQuestFile.openGui() != null) {
                InputConstants.grabOrReleaseMouse(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CURSOR_NORMAL, mx, my);
            }
            return EventResult.interruptFalse();
        }

        return EventResult.pass();
    }

    private void onClientTick(Minecraft mc) {
        if (mc.level != null && ClientQuestFile.exists() && mc.player != null) {
            PinnedQuestsTracker.INSTANCE.tick(ClientQuestFile.INSTANCE);

            observationHandler.tick(mc);
        }
    }

    private void onPlayerLogin(LocalPlayer localPlayer) {
        if (creativeTabRebuildPending) {
            FTBQuestsClient.rebuildCreativeTabs();
            creativeTabRebuildPending = false;
        }

        String locale = FTBQuestsClientConfig.EDITING_LOCALE.get();
        if (!locale.isEmpty() && !locale.equals(Minecraft.getInstance().options.languageCode)) {
            NetworkManager.sendToServer(new RequestTranslationTableMessage(locale));
        }
    }

    private void onPlayerLogout(@Nullable LocalPlayer localPlayer) {
        StructureTask.syncKnownStructureList(List.of());
    }

    private void onScreenRender(GuiGraphics graphics, DeltaTracker tickDelta) {
        if (!ClientQuestFile.exists()) {
            return;
        }

        observationHandler.render(Minecraft.getInstance(), graphics, tickDelta);

        PinnedQuestsTracker.INSTANCE.render(Minecraft.getInstance(), graphics);
    }
}
