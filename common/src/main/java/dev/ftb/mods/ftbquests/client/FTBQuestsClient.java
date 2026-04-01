package dev.ftb.mods.ftbquests.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableEntityFace;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableImageResource;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableResource;
import dev.ftb.mods.ftblibrary.client.config.gui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.platform.client.PlatformClient;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.block.entity.BaseBarrierBlockEntity;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.client.gui.CustomToast;
import dev.ftb.mods.ftbquests.client.gui.RewardSelectorScreen;
import dev.ftb.mods.ftbquests.client.gui.RewardToast;
import dev.ftb.mods.ftbquests.client.gui.ToastQuestObject;
import dev.ftb.mods.ftbquests.item.CustomIconItem;
import dev.ftb.mods.ftbquests.net.SetCustomImageMessage;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.theme.ThemeLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class FTBQuestsClient {
	public static final Identifier GUI_OVERLAY_ID = FTBQuestsAPI.id("tracked_quests");

	private static final KeyMapping.Category FTB_QUESTS_KEY_CATEGORY
			= new KeyMapping.Category(FTBQuestsAPI.id("keys"));
	public static final KeyMapping KEY_QUESTS
			= new KeyMapping("key.ftbquests.quests", InputConstants.Type.KEYSYM, -1, FTB_QUESTS_KEY_CATEGORY);

	public final FTBQuestsClientEventHandler eventHandler;

	public FTBQuestsClient() {
		ConfigManager.getInstance().registerClientConfig(FTBQuestsClientConfig.CONFIG, FTBQuestsAPI.MOD_ID, FTBQuestsClientConfig::onEdited);

		PlatformClient.get().addResourcePackReloadListeners(FTBQuestsAPI.MOD_ID, Map.of(
				FTBQuestsAPI.id("file_cache"), new QuestFileCacheReloader(),
				FTBQuestsAPI.id("themes"), new ThemeLoader()
		));

		PlatformClient.get().registerKeyMapping(FTBQuestsAPI.MOD_ID, KEY_QUESTS);

		eventHandler = new FTBQuestsClientEventHandler();
	}


	public void onClientSetup(Minecraft minecraft) {
//		RenderTypeRegistry.register(ChunkSectionLayer.TRANSLUCENT, ModBlocks.BARRIER.get());
//		RenderTypeRegistry.register(ChunkSectionLayer.TRANSLUCENT, ModBlocks.STAGE_BARRIER.get());
//		RenderTypeRegistry.register(ChunkSectionLayer.SOLID, ModBlocks.TASK_SCREEN_1.get());
//		RenderTypeRegistry.register(ChunkSectionLayer.SOLID, ModBlocks.TASK_SCREEN_3.get());
//		RenderTypeRegistry.register(ChunkSectionLayer.SOLID, ModBlocks.TASK_SCREEN_5.get());
//		RenderTypeRegistry.register(ChunkSectionLayer.SOLID, ModBlocks.TASK_SCREEN_7.get());
//		RenderTypeRegistry.register(ChunkSectionLayer.SOLID, ModBlocks.AUX_SCREEN.get());
		GuiProviders.setTaskGuiProviders();
		GuiProviders.setRewardGuiProviders();
	}

	@Nullable
	public static BaseQuestFile getClientQuestFile() {
		return ClientQuestFile.getInstance();
	}

//	public static Player getClientPlayer() {
//		return Objects.requireNonNull(Minecraft.getInstance().player);
//	}
//
//	public static Level getClientLevel() {
//		return Objects.requireNonNull(Minecraft.getInstance().level);
//	}

	public static boolean isClientDataLoaded() {
		return ClientQuestFile.exists();
	}

	public static TeamData getClientPlayerData() {
		return Objects.requireNonNull(ClientQuestFile.getInstance().selfTeamData);
	}

	public static BaseQuestFile createClientQuestFile() {
		return new ClientQuestFile();
	}

	public static HolderLookup.Provider holderLookup() {
		return ClientUtils.getClientLevel().registryAccess();
	}

	public static void openGui() {
		ClientQuestFile.openGui();
	}

	public static void openCustomIconGui(Player player, InteractionHand hand) {
		EditableResource<?> editable = Minecraft.getInstance().hasShiftDown() ? new EditableEntityFace() : new EditableImageResource();
		editable.onClicked(null, MouseButton.LEFT, accepted -> {
			if (accepted) {
				// TODO minor code smell here
				if (editable.getValue() instanceof Identifier id) {
					CustomIconItem.setIcon(player.getItemInHand(hand), editable.isEmpty() ? null : id);
					Play2ServerNetworking.send(new SetCustomImageMessage(hand, false, id));
				} else if (editable.getValue() instanceof EntityType<?> et) {
					CustomIconItem.setFaceIcon(player.getItemInHand(hand), editable.isEmpty() ? EditableEntityFace.NONE : et);
					Play2ServerNetworking.send(new SetCustomImageMessage(hand, true, BuiltInRegistries.ENTITY_TYPE.getKey(et)));
				}
			}
			Minecraft.getInstance().setScreen(null);
		});
	}

	public static void openTaskScreenConfigGui(BlockPos pos) {
		if (ClientUtils.getClientLevel().getBlockEntity(pos) instanceof TaskScreenBlockEntity coreScreen) {
			new EditConfigScreen(coreScreen.fillConfigGroup(ClientQuestFile.getInstance().getOrCreateTeamData(coreScreen.getTeamId()))).setAutoclose(true).openGui();
		}
	}

	public static void openBarrierConfigGui(BlockPos pos) {
		if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof BaseBarrierBlockEntity barrier) {
			new EditConfigScreen(barrier.fillConfigGroup()).setAutoclose(true).openGui();
		}
	}

	// TODO how does this work now in 26.1?
	public static float[] getTextureUV(@Nullable BlockState state, Direction face) {
		if (state == null) return new float[0];
//		BlockStateModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
//		List<BlockModelPart> blockModelParts = model.collectParts(RandomSource.create());
//		List<BakedQuad> quads = blockModelParts.getFirst().getQuads(face);
//		if (!quads.isEmpty()) {
//			TextureAtlasSprite sprite = quads.getFirst().sprite();
//			return new float[] { sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1() };
//		} else {
//			return new float[0];
//		}
		return new float[0];
	}

	/**
	 * Called to ensure the right loot crates are shown; call when a loot crate is toggled via GUI, or when a
	 * quest book sync is done from the server.
	 */
	public static void rebuildCreativeTabs() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			// It's possible for the client player to still be null here, since quest book sync can be processed
			// before the player is fully logged in to the client. In that case, we'll just mark the creative tabs
			// as rebuild-needed and do it when the player logs in
			FTBQuests.LOGGER.debug("deferring creative tab rebuild, client player still null");
			FTBQuestsClientEventHandler.creativeTabRebuildPending = true;
		} else {
			FTBQuests.LOGGER.debug("rebuilding creative tabs now");
			CreativeModeTab.ItemDisplayParameters params = new CreativeModeTab.ItemDisplayParameters(
					player.connection.enabledFeatures(),
					player.canUseGameMasterBlocks(),
					player.level().registryAccess()
			);
			FTBLibrary.getCreativeModeTab().get().buildContents(params);
			CreativeModeTabs.searchTab().buildContents(params);
		}
	}

	public static Optional<CreativeModeTab.ItemDisplayParameters> creativeTabDisplayParams() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			return Optional.of(new CreativeModeTab.ItemDisplayParameters(player.connection.enabledFeatures(), Minecraft.getInstance().options.operatorItemsTab().get(), player.level().registryAccess()));
		}
		return Optional.empty();
	}

	public static void copyToClipboard(QuestObjectBase qo) {
		Widget.setClipboardString(qo.getCodeString());
	}

	public static void copyToClipboard(String str) {
		Widget.setClipboardString(str);
	}

	static void showCompletionToast(QuestObject qo) {
		Minecraft.getInstance().getToastManager().addToast(new ToastQuestObject(qo));
	}

	static void showRewardToast(Component text, Icon<?> icon) {
		Screen screen = Minecraft.getInstance().screen;
		if (screen == null || ClientUtils.getGuiAs(screen, RewardSelectorScreen.class) == null) {
			Minecraft.getInstance().getToastManager().addToast(new RewardToast(text, icon));
		}
	}

	public static void showErrorToast(Component text) {
		showErrorToast(text, Component.empty());
	}

	public static void showErrorToast(Component text, Component desc) {
		Minecraft.getInstance().getToastManager().addToast(new CustomToast(text, Icons.BARRIER, desc));
	}

	public static void showInfoToast(Component text, Component desc) {
		Minecraft.getInstance().getToastManager().addToast(new CustomToast(text, Icons.INFO, desc));
	}
}
