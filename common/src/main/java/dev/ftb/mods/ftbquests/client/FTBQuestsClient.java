package dev.ftb.mods.ftbquests.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.ftb.mods.ftblibrary.config.*;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigFromStringScreen;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.config.ui.SelectFluidScreen;
import dev.ftb.mods.ftblibrary.config.ui.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.block.FTBQuestsBlocks;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import dev.ftb.mods.ftbquests.net.SetCustomImageMessage;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.reward.XPLevelsReward;
import dev.ftb.mods.ftbquests.quest.reward.XPReward;
import dev.ftb.mods.ftbquests.quest.task.*;
import dev.ftb.mods.ftbquests.quest.theme.ThemeLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class FTBQuestsClient {
	public static KeyMapping KEY_QUESTS;

	public static void init() {
		ClientLifecycleEvent.CLIENT_SETUP.register(FTBQuestsClient::onClientSetup);
		ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, new QuestFileCacheReloader());
		ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, new ThemeLoader());
		KeyMappingRegistry.register(KEY_QUESTS = new KeyMapping("key.ftbquests.quests", InputConstants.Type.KEYSYM, -1, "key.categories.ftbquests"));
		new FTBQuestsClientEventHandler().init();
	}

	private static void onClientSetup(Minecraft minecraft) {
		RenderTypeRegistry.register(RenderType.translucent(), FTBQuestsBlocks.BARRIER.get());
		RenderTypeRegistry.register(RenderType.translucent(), FTBQuestsBlocks.STAGE_BARRIER.get());
		RenderTypeRegistry.register(RenderType.solid(), FTBQuestsBlocks.TASK_SCREEN_1.get());
		RenderTypeRegistry.register(RenderType.solid(), FTBQuestsBlocks.TASK_SCREEN_3.get());
		RenderTypeRegistry.register(RenderType.solid(), FTBQuestsBlocks.TASK_SCREEN_5.get());
		RenderTypeRegistry.register(RenderType.solid(), FTBQuestsBlocks.TASK_SCREEN_7.get());
		RenderTypeRegistry.register(RenderType.solid(), FTBQuestsBlocks.AUX_SCREEN.get());
		setTaskGuiProviders();
		setRewardGuiProviders();
	}

	@Nullable
	public static BaseQuestFile getClientQuestFile() {
		return ClientQuestFile.INSTANCE;
	}

	public static void setTaskGuiProviders() {
		TaskTypes.ITEM.setGuiProvider((gui, quest, callback) -> {
			ItemStackConfig c = new ItemStackConfig(false, false);

			new SelectItemStackScreen(c, accepted -> {
				gui.run();
				if (accepted) {
					ItemTask itemTask = new ItemTask(0L, quest).setStackAndCount(c.getValue(), c.getValue().getCount());
					callback.accept(itemTask);
				}
			}).openGui();
		});

		TaskTypes.CHECKMARK.setGuiProvider((gui, quest, callback) -> {
			StringConfig c = new StringConfig(null);

			EditConfigFromStringScreen.open(c, "", "", accepted -> {
				if (accepted) {
					CheckmarkTask checkmarkTask = new CheckmarkTask(0L, quest);
					checkmarkTask.setRawTitle(c.getValue());
					callback.accept(checkmarkTask);
				}

				gui.run();
			});
		});

        TaskTypes.FLUID.setGuiProvider((gui, quest, callback) -> {
			FluidConfig c = new FluidConfig(false);

			new SelectFluidScreen(c, accepted -> {
				gui.run();
				if (accepted) {
					FluidTask fluidTask = new FluidTask(0L, quest).setFluid(c.getValue().getFluid());
					callback.accept(fluidTask);
				}
			}).openGui();
		});

		TaskTypes.DIMENSION.setGuiProvider((gui, quest, callback) -> {
			DimensionTask task = new DimensionTask(0L, quest)
					.withDimension(Minecraft.getInstance().level.dimension());
			openSetupGui(gui, callback, task);
		});

		TaskTypes.OBSERVATION.setGuiProvider((gui, quest, callback) -> {
			ObservationTask task = new ObservationTask(0L, quest);
			if (Minecraft.getInstance().hitResult instanceof BlockHitResult bhr) {
				Block block = Minecraft.getInstance().level.getBlockState(bhr.getBlockPos()).getBlock();
				task.setToObserve(BuiltInRegistries.BLOCK.getKey(block).toString());
			}
			openSetupGui(gui, callback, task);
		});

		TaskTypes.LOCATION.setGuiProvider((gui, quest, callback) -> {
			LocationTask task = new LocationTask(0L, quest);
			Minecraft mc = Minecraft.getInstance();

			if (mc.hitResult instanceof BlockHitResult bhr) {
				var blockEntity = mc.level.getBlockEntity(bhr.getBlockPos());

				if (blockEntity instanceof StructureBlockEntity structure) {
					task.initFromStructure(structure);
					callback.accept(task);
					return;
				}
			}

			openSetupGui(gui, callback, task);
		});
	}

	private static void openSetupGui(Runnable gui, Consumer<Task> callback, Task task) {
		ConfigGroup group = new ConfigGroup(FTBQuestsAPI.MOD_ID, accepted -> {
			gui.run();
			if (accepted) {
				callback.accept(task);
			}
		});
		task.fillConfigGroup(task.createSubGroup(group));

		new EditConfigScreen(group).openGui();
	}

	public static void setRewardGuiProviders() {
		RewardTypes.ITEM.setGuiProvider((gui, quest, callback) -> {
			ItemStackConfig c = new ItemStackConfig(false, false);

			new SelectItemStackScreen(c, accepted -> {
				if (accepted) {
					ItemStack copy = c.getValue().copy();
					copy.setCount(1);
					ItemReward reward = new ItemReward(0L, quest, copy, c.getValue().getCount());
					callback.accept(reward);
				}
				gui.run();
			}).openGui();
		});

		RewardTypes.XP.setGuiProvider((gui, quest, callback) -> {
			IntConfig c = new IntConfig(1, Integer.MAX_VALUE);

			EditConfigFromStringScreen.open(c, 100, 100, accepted -> {
				if (accepted) {
					callback.accept(new XPReward(0L, quest, c.getValue()));
				}

				gui.run();
			});
		});

		RewardTypes.XP_LEVELS.setGuiProvider((gui, quest, callback) -> {
			IntConfig c = new IntConfig(1, Integer.MAX_VALUE);

			EditConfigFromStringScreen.open(c, 5, 5, accepted -> {
				if (accepted) {
					callback.accept(new XPLevelsReward(0L, quest, c.getValue()));
				}

				gui.run();
			});
		});
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

	public static void openGui() {
		ClientQuestFile.openGui();
	}

	public static void openCustomIconGui(Player player, InteractionHand hand) {
		ImageConfig config = new ImageConfig();
		config.onClicked(MouseButton.LEFT, b -> {
			if (b) {
				if (config.getValue().isEmpty()) {
					player.getItemInHand(hand).removeTagKey("Icon");
				} else {
					player.getItemInHand(hand).addTagElement("Icon", StringTag.valueOf(config.getValue()));
				}

				new SetCustomImageMessage(hand, config.getValue()).sendToServer();
			}

			Minecraft.getInstance().setScreen(null);
		});
	}

	public static void openScreenConfigGui(BlockPos pos) {
		if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof TaskScreenBlockEntity coreScreen) {
			new EditConfigScreen(coreScreen.fillConfigGroup(ClientQuestFile.INSTANCE.getOrCreateTeamData(coreScreen.getTeamId()))).setAutoclose(true).openGui();
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
			FTBQuestsItems.CREATIVE_TAB.get().buildContents(params);
			CreativeModeTabs.searchTab().buildContents(params);
		}
	}

	public static Optional<RegistryAccess> registryAccess() {
		return Minecraft.getInstance().level == null ? Optional.empty() : Optional.of(Minecraft.getInstance().level.registryAccess());
	}

	public static void copyToClipboard(QuestObjectBase qo) {
		Widget.setClipboardString(qo.getCodeString());
	}
}