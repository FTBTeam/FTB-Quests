package dev.ftb.mods.ftbquests.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.registries.RegistrarManager;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.EntityFaceConfig;
import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftblibrary.config.ResourceConfigValue;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.block.entity.BaseBarrierBlockEntity;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.client.gui.RewardToast;
import dev.ftb.mods.ftbquests.client.gui.ToastQuestObject;
import dev.ftb.mods.ftbquests.item.CustomIconItem;
import dev.ftb.mods.ftbquests.net.SetCustomImageMessage;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.theme.ThemeLoader;
import dev.ftb.mods.ftbquests.registry.ModBlocks;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class FTBQuestsClient {
	public static KeyMapping KEY_QUESTS;

    public static void init() {
		maybeMigrateClientConfig();

		ConfigManager.getInstance().registerClientConfig(FTBQuestsClientConfig.CONFIG, FTBQuestsAPI.MOD_ID, FTBQuestsClientConfig::onEdited);

		ClientLifecycleEvent.CLIENT_SETUP.register(FTBQuestsClient::onClientSetup);

		// Minecraft.getInstance() might not exist here (datagen in particular)
        //noinspection ConstantValue
        if (Minecraft.getInstance() != null) {
			ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, new QuestFileCacheReloader());
			ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, new ThemeLoader());
			KeyMappingRegistry.register(KEY_QUESTS = new KeyMapping("key.ftbquests.quests", InputConstants.Type.KEYSYM, -1, "key.categories.ftbquests"));
		}

		new FTBQuestsClientEventHandler().init();
	}

	private static void maybeMigrateClientConfig() {
		// TODO delete in 1.22
		Path oldConfig = Platform.getGameFolder().resolve("local/ftbquests/client-config.snbt");
		Path newConfig = Platform.getConfigFolder().resolve("ftbquests-client.snbt");

		if (Files.exists(oldConfig) && !Files.exists(newConfig)) {
			try {
				Files.move(oldConfig, newConfig);
				FTBQuests.LOGGER.info("migrated {} to {}", oldConfig, newConfig);
			} catch (IOException e) {
				FTBQuests.LOGGER.error("can't migrate {} to {}: {}", oldConfig, newConfig, e.getMessage());
			}
		}
	}

	private static void onClientSetup(Minecraft minecraft) {
		RenderTypeRegistry.register(RenderType.translucent(), ModBlocks.BARRIER.get());
		RenderTypeRegistry.register(RenderType.translucent(), ModBlocks.STAGE_BARRIER.get());
		RenderTypeRegistry.register(RenderType.solid(), ModBlocks.TASK_SCREEN_1.get());
		RenderTypeRegistry.register(RenderType.solid(), ModBlocks.TASK_SCREEN_3.get());
		RenderTypeRegistry.register(RenderType.solid(), ModBlocks.TASK_SCREEN_5.get());
		RenderTypeRegistry.register(RenderType.solid(), ModBlocks.TASK_SCREEN_7.get());
		RenderTypeRegistry.register(RenderType.solid(), ModBlocks.AUX_SCREEN.get());
		GuiProviders.setTaskGuiProviders();
		GuiProviders.setRewardGuiProviders();
	}

	@Nullable
	public static BaseQuestFile getClientQuestFile() {
		return ClientQuestFile.INSTANCE;
	}

	public static Player getClientPlayer() {
		return Minecraft.getInstance().player;
	}

	public static Level getClientLevel() {
		return Minecraft.getInstance().level;
	}

	public static boolean isClientDataLoaded() {
		return ClientQuestFile.exists();
	}

	public static TeamData getClientPlayerData() {
		return ClientQuestFile.INSTANCE.selfTeamData;
	}

	public static BaseQuestFile createClientQuestFile() {
		return new ClientQuestFile();
	}

	public static HolderLookup.Provider holderLookup() {
		return getClientLevel().registryAccess();
	}

	public static void openGui() {
		ClientQuestFile.openGui();
	}

	public static void openCustomIconGui(Player player, InteractionHand hand) {
		ResourceConfigValue<?> config = Screen.hasShiftDown() ? new EntityFaceConfig() : new ImageResourceConfig();
		config.onClicked(null, MouseButton.LEFT, accepted -> {
			if (accepted) {
				// TODO minor code smell here
				if (config.getValue() instanceof ResourceLocation rl) {
					CustomIconItem.setIcon(player.getItemInHand(hand), config.isEmpty() ? null : rl);
					NetworkManager.sendToServer(new SetCustomImageMessage(hand, false, rl));
				} else if (config.getValue() instanceof EntityType<?> et) {
					CustomIconItem.setFaceIcon(player.getItemInHand(hand), config.isEmpty() ? null : et);
					NetworkManager.sendToServer(new SetCustomImageMessage(hand, true, RegistrarManager.getId(et, Registries.ENTITY_TYPE)));
				}
			}
			Minecraft.getInstance().setScreen(null);
		});
	}

	public static void openTaskScreenConfigGui(BlockPos pos) {
		if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof TaskScreenBlockEntity coreScreen) {
			new EditConfigScreen(coreScreen.fillConfigGroup(ClientQuestFile.INSTANCE.getOrCreateTeamData(coreScreen.getTeamId()))).setAutoclose(true).openGui();
		}
	}

	public static void openBarrierConfigGui(BlockPos pos) {
		if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof BaseBarrierBlockEntity barrier) {
			new EditConfigScreen(barrier.fillConfigGroup()).setAutoclose(true).openGui();
		}
	}

	public static float[] getTextureUV(BlockState state, Direction face) {
		if (state == null) return null;
		BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
		List<BakedQuad> quads = model.getQuads(state, face, RandomSource.create());
		if (!quads.isEmpty()) {
			TextureAtlasSprite sprite = quads.get(0).getSprite();
			return new float[] { sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1() };
		} else {
			return new float[0];
		}
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
			return Optional.of(new CreativeModeTab.ItemDisplayParameters(player.connection.enabledFeatures(), Minecraft.getInstance().options.operatorItemsTab().get(), player.clientLevel.registryAccess()));
		}
		return Optional.empty();
	}

	public static void copyToClipboard(QuestObjectBase qo) {
		Widget.setClipboardString(qo.getCodeString());
	}

	static void showCompletionToast(QuestObject qo) {
		Minecraft.getInstance().getToasts().addToast(new ToastQuestObject(qo));
	}

	static void showRewardToast(Component text, Icon icon) {
		Minecraft.getInstance().getToasts().addToast(new RewardToast(text, icon));
	}
}
