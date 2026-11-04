package dev.ftb.mods.ftbquests.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ftb.mods.ftblibrary.api.event.client.CustomClickEvent;
import dev.ftb.mods.ftblibrary.api.event.client.SidebarButtonCreatedEvent;
import dev.ftb.mods.ftblibrary.api.sidebar.ButtonOverlayRender;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.api.event.ClearFileCacheEvent;
import dev.ftb.mods.ftbquests.item.LootCrateItem;
import dev.ftb.mods.ftbquests.net.RequestTranslationTableMessage;
import dev.ftb.mods.ftbquests.quest.task.StructureTask;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static dev.ftb.mods.ftbquests.client.TaskScreenRenderer.*;

public class FTBQuestsClientEventHandler {
    private static final Identifier QUESTS_BUTTON = FTBQuestsAPI.id("quests");

    static boolean creativeTabRebuildPending = false;

    private final ObservationClientHandler observationHandler = new ObservationClientHandler();

    @Nullable
    public static TextureAtlasSprite inputOnlySprite;
    @Nullable
    public static TextureAtlasSprite tankSprite;
    @Nullable
    public static TextureAtlasSprite feEnergyEmptySprite;
    @Nullable
    public static TextureAtlasSprite feEnergyFullSprite;
    @Nullable
    public static TextureAtlasSprite trEnergyEmptySprite;
    @Nullable
    public static TextureAtlasSprite trEnergyFullSprite;

    public void init() {
        ItemTintSources.ID_MAPPER.put(FTBQuestsAPI.id("loot_crate"), LootCrateItem.LootCrateItemTintSource.MAP_CODEC);
    }

    public void onClientSetup(Minecraft ignoredMinecraft) {
        GuiProviders.setTaskGuiProviders();
        GuiProviders.setRewardGuiProviders();
    }

    public static void onTextureStitchPost(TextureAtlas textureAtlas) {
        if (textureAtlas.location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            inputOnlySprite = textureAtlas.getSprite(INPUT_ONLY_TEXTURE);
            tankSprite = textureAtlas.getSprite(TANK_TEXTURE);
            feEnergyEmptySprite = textureAtlas.getSprite(FE_ENERGY_EMPTY_TEXTURE);
            feEnergyFullSprite = textureAtlas.getSprite(FE_ENERGY_FULL_TEXTURE);
            trEnergyEmptySprite = textureAtlas.getSprite(TR_ENERGY_EMPTY_TEXTURE);
            trEnergyFullSprite = textureAtlas.getSprite(TR_ENERGY_FULL_TEXTURE);
        }
    }

    public void onSidebarButtonCreated(SidebarButtonCreatedEvent.Data event) {
        if (event.button().getId().equals(QUESTS_BUTTON)) {
            event.button().addOverlayRender(ButtonOverlayRender.ofSimpleString(() -> {
                if (ClientQuestFile.exists()) {
                    if (ClientQuestFile.getInstance().isDisableGui() && !ClientQuestFile.getInstance().canEdit()
                            || FTBQuestsClient.getClientPlayerData().isLocked()) {
                        return "[X]";
                    } else if (FTBQuestsClient.getClientPlayerData().hasUnclaimedRewards(ClientUtils.getClientPlayer().getUUID(), ClientQuestFile.getInstance())) {
                        return "[!]";
                    }
                }
                return "";
            }));
        }
    }

    public void onFileCacheClear(ClearFileCacheEvent.Data data) {
        if (!data.file().isServerSide()) {
            observationHandler.clearCachedData();
        }
    }

    public void onKeyEvent(Minecraft minecraft) {
        if (ClientQuestFile.exists()
                && (!ClientQuestFile.getInstance().isDisableGui() || ClientQuestFile.getInstance().canEdit())
                && FTBQuestsKeyMappings.KEY_QUESTS.consumeClick()) {
            ClientQuestFile.openGui();
        } else if (FTBQuestsKeyMappings.KEY_CYCLE_PINNED_TRACKER.consumeClick()) {
            FTBQuestsClientConfig.cyclePinnedTrackerVisibility();
        }
    }

    public boolean onCustomClick(CustomClickEvent.Data event) {
        if (event.id().getNamespace().equals(FTBQuestsAPI.MOD_ID) && "open_gui".equals(event.id().getPath())) {
            // to be safe, we close the current screen before opening Quests, to avoid potential gui open-close loops with other mods
            // also save the cursor position and restore it after, since closing the screen will reset to the centre
            double mx = Minecraft.getInstance().mouseHandler.xpos();
            double my = Minecraft.getInstance().mouseHandler.ypos();
            Minecraft.getInstance().setScreen(null);
            if (ClientQuestFile.openGui() != null) {
                InputConstants.grabOrReleaseMouse(Minecraft.getInstance().getWindow(), GLFW.GLFW_CURSOR_NORMAL, mx, my);
            }
            return true;
        }

        return false;
    }

    public void onClientTick(Minecraft mc) {
        if (mc.level != null && ClientQuestFile.exists() && mc.player != null) {
            PinnedQuestsTracker.INSTANCE.tick(ClientQuestFile.getInstance());

            observationHandler.tick(mc);
        }
    }

    public void onPlayerLogin() {
        if (creativeTabRebuildPending) {
            FTBQuestsClient.rebuildCreativeTabs();
            creativeTabRebuildPending = false;
        }

        String locale = FTBQuestsClientConfig.EDITING_LOCALE.get();
        if (!locale.isEmpty() && !locale.equals(Minecraft.getInstance().options.languageCode)) {
            Play2ServerNetworking.send(new RequestTranslationTableMessage(locale));
        }
    }

    public void onPlayerLogout() {
        StructureTask.syncKnownStructureList(List.of());
    }

    public void renderGuiOverlay(GuiGraphicsExtractor graphics, DeltaTracker tickDelta) {
        if (ClientQuestFile.exists()) {
            observationHandler.render(Minecraft.getInstance(), graphics, tickDelta);
            PinnedQuestsTracker.INSTANCE.render(Minecraft.getInstance(), graphics);
        }
    }
}
