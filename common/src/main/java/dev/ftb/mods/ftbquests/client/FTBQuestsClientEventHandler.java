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
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.sidebar.SidebarButtonCreatedEvent;
import dev.ftb.mods.ftblibrary.ui.CustomClickEvent;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.events.ClearFileCacheEvent;
import dev.ftb.mods.ftbquests.item.LootCrateItem;
import dev.ftb.mods.ftbquests.net.RequestTranslationTableMessage;
import dev.ftb.mods.ftbquests.net.SubmitTaskMessage;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.task.ObservationTask;
import dev.ftb.mods.ftbquests.quest.task.StructureTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbquests.registry.ModItems;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.ftb.mods.ftbquests.client.TaskScreenRenderer.*;

public class FTBQuestsClientEventHandler {
	private static final ResourceLocation QUESTS_BUTTON = FTBQuestsAPI.rl("quests");

	static boolean creativeTabRebuildPending = false;

	private List<ObservationTask> observationTasks = null;
	private ObservationTask currentlyObserving = null;
	private long currentlyObservingTicks = 0L;
	private final List<FormattedCharSequence> pinnedQuestText = new ArrayList<>();

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
			LootCrate crate = LootCrateItem.getCrate(stack);
			return crate == null ? 0xFFFFFFFF : (0xFF000000 | crate.getColor().rgb());
		}, ModItems.LOOTCRATE.get());
	}

	private void onSidebarButtonCreated(SidebarButtonCreatedEvent event) {
		if (event.getButton().getId().equals(QUESTS_BUTTON)) {
			event.getButton().setCustomTextHandler(() ->
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
			});
		}
	}

	private void onFileCacheClear(BaseQuestFile file) {
		if (!file.isServerSide()) {
			observationTasks = null;
		}
	}

	private void onKeyEvent(Minecraft mc) {
		if (ClientQuestFile.exists()
				&& (!ClientQuestFile.INSTANCE.isDisableGui() || ClientQuestFile.INSTANCE.canEdit())
				&& FTBQuestsClient.KEY_QUESTS.consumeClick())
		{
			ClientQuestFile.openGui();
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
			collectPinnedQuests(ClientQuestFile.INSTANCE);

			if (observationTasks == null) {
				observationTasks = ClientQuestFile.INSTANCE.collect(ObservationTask.class);
			}

			if (observationTasks.isEmpty()) {
				return;
			}

			currentlyObserving = null;

			TeamData selfTeamData = ClientQuestFile.INSTANCE.selfTeamData;
			if (mc.hitResult != null && mc.hitResult.getType() != HitResult.Type.MISS) {
				for (ObservationTask task : observationTasks) {
					if (!selfTeamData.isCompleted(task) && task.observe(mc.player, mc.hitResult)
							&& selfTeamData.canStartTasks(task.getQuest())) {
						currentlyObserving = task;
						break;
					}
				}
			}

			if (currentlyObserving != null) {
				if (!mc.isPaused()) {
					currentlyObservingTicks++;
				}

				if (currentlyObservingTicks >= currentlyObserving.getTimer()) {
					NetworkManager.sendToServer(new SubmitTaskMessage(currentlyObserving.id));
					selfTeamData.addProgress(currentlyObserving, 1L);
					currentlyObserving = null;
					currentlyObservingTicks = 0L;
				}
			} else {
				currentlyObservingTicks = 0L;
			}
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

	private void collectPinnedQuests(ClientQuestFile file) {
		TeamData data = file.selfTeamData;

		List<Quest> pinnedQuests = new ArrayList<>();
		LongSet pinnedIds = data.getPinnedQuestIds(FTBQuestsClient.getClientPlayer());
		if (!pinnedIds.isEmpty()) {
			if (pinnedIds.contains(TeamData.AUTO_PIN_ID)) {
				// special auto-pin value: collect all quests which can be done now
				file.forAllQuests(quest -> {
					if (!data.isCompleted(quest) && data.canStartTasks(quest)) {
						pinnedQuests.add(quest);
					}
				});
			} else {
				pinnedIds.longStream()
						.mapToObj(file::getQuest)
						.filter(Objects::nonNull)
						.forEach(pinnedQuests::add);
			}
		}

		Minecraft mc = Minecraft.getInstance();

		pinnedQuestText.clear();

		for (int i = 0; i < pinnedQuests.size(); i++) {
			Quest quest = pinnedQuests.get(i);

			if (i > 0) pinnedQuestText.add(FormattedCharSequence.EMPTY);  // separator line between quests

			pinnedQuestText.addAll(mc.font.split(FormattedText.composite(
					mc.font.getSplitter().headByWidth(quest.getTitle(), 160, Style.EMPTY.withBold(true)),
					Component.literal(" ")
							.withStyle(ChatFormatting.DARK_AQUA)
							.append(data.getRelativeProgress(quest) + "%")
			), 500));

			for (Task task : quest.getTasks()) {
				if (!data.isCompleted(task)) {
					pinnedQuestText.addAll(mc.font.split(FormattedText.composite(
							mc.font.getSplitter().headByWidth(task.getMutableTitle().withStyle(ChatFormatting.GRAY), 160, Style.EMPTY.applyFormat(ChatFormatting.GRAY)),
							Component.literal(" ")
									.withStyle(ChatFormatting.GREEN)
									.append(task.formatProgress(data, data.getProgress(task)))
									.append("/")
									.append(task.formatMaxProgress())
					), 500));
				}
			}
		}

	}

	private void onScreenRender(GuiGraphics graphics, DeltaTracker tickDelta) {
		if (!ClientQuestFile.exists()) {
			return;
		}

		if (currentlyObserving != null) {
			renderCurrentlyObserving(Minecraft.getInstance(), graphics, tickDelta);
		}

		if (!pinnedQuestText.isEmpty()) {
			renderPinnedQuestPanel(Minecraft.getInstance(), graphics);
		}
	}

	private void renderCurrentlyObserving(Minecraft mc, GuiGraphics graphics, DeltaTracker tickDelta) {
		int cx = mc.getWindow().getGuiScaledWidth() / 2;
		int cy = mc.getWindow().getGuiScaledHeight() / 2;

		MutableComponent txt = currentlyObserving.getMutableTitle().withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE);
		int txtWidth = mc.font.width(txt);
		int boxWidth = Math.max(txtWidth, 100);

		Color4I.DARK_GRAY.withAlpha(130).draw(graphics, cx - boxWidth / 2 - 3, cy - 63, boxWidth + 6, 29);
		GuiHelper.drawHollowRect(graphics, cx - boxWidth / 2 - 3, cy - 63, boxWidth + 6, 29, Color4I.DARK_GRAY, false);

		graphics.drawString(mc.font, txt, cx - txtWidth / 2, cy - 60, 0xFFFFFF);
		double completed = (currentlyObservingTicks + tickDelta.getGameTimeDeltaPartialTick(false)) / (double) currentlyObserving.getTimer();

		GuiHelper.drawHollowRect(graphics, cx - boxWidth / 2, cy - 49, boxWidth, 12, Color4I.DARK_GRAY, false);
		Color4I.LIGHT_BLUE.withAlpha(130).draw(graphics, cx - boxWidth / 2 + 1, cy - 48, (int) ((boxWidth - 2D) * completed), 10);

		String pctTxt = (currentlyObservingTicks * 100L / currentlyObserving.getTimer()) + "%";
		graphics.drawString(mc.font, pctTxt, cx - mc.font.width(pctTxt) / 2, cy - 47, 0xFFFFFF);
	}

	private void renderPinnedQuestPanel(Minecraft mc, GuiGraphics graphics) {
		int width = 0;
		for (FormattedCharSequence s : pinnedQuestText) {
			width = Math.max(width, (int) mc.font.getSplitter().stringWidth(s));
		}
		width += 8;
		int height = mc.font.lineHeight * pinnedQuestText.size() + 8;

		float scale = ThemeProperties.PINNED_QUEST_SIZE.get(ClientQuestFile.INSTANCE).floatValue();

		int insetX = FTBQuestsClientConfig.PINNED_QUESTS_INSET_X.get();
		int insetY = FTBQuestsClientConfig.PINNED_QUESTS_INSET_Y.get();
		var pos = FTBQuestsClientConfig.PINNED_QUESTS_POS.get().getPanelPos(
				mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(),
				(int) (width * scale), (int) (height * scale),
				insetX, insetY
		);

		graphics.pose().pushPose();
		graphics.pose().translate(pos.x(), pos.y(), 100);
		graphics.pose().scale(scale, scale, 1F);

		GuiHelper.drawHollowRect(graphics, 0, 0, width, height, Color4I.BLACK.withAlpha(100), false);
		Color4I.BLACK.withAlpha(100).draw(graphics, 0, 0, width, height);

		graphics.pose().translate(4, 4, 0);
		for (int i = 0; i < pinnedQuestText.size(); i++) {
			graphics.drawString(mc.font, pinnedQuestText.get(i), 0, i * mc.font.lineHeight, 0xFFFFFFFF);
		}

		graphics.pose().popPose();
	}
}
