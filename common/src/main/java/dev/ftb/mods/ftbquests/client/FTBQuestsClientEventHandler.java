package dev.ftb.mods.ftbquests.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.*;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.sidebar.SidebarButtonCreatedEvent;
import dev.ftb.mods.ftblibrary.ui.CustomClickEvent;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.block.entity.FTBQuestsBlockEntities;
import dev.ftb.mods.ftbquests.events.ClearFileCacheEvent;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import dev.ftb.mods.ftbquests.item.LootCrateItem;
import dev.ftb.mods.ftbquests.net.SubmitTaskMessage;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.task.ObservationTask;
import dev.ftb.mods.ftbquests.quest.task.StructureTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
import java.util.function.Consumer;

import static dev.ftb.mods.ftbquests.client.TaskScreenRenderer.*;

public class FTBQuestsClientEventHandler {
	private static final ResourceLocation QUESTS_BUTTON = new ResourceLocation(FTBQuests.MOD_ID, "quests");

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
		ClientTextureStitchEvent.PRE.register(this::onTextureStitchPre);
		ClientTextureStitchEvent.POST.register(this::onTextureStitchPost);
		ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(this::onPlayerLogout);
	}

	private void onTextureStitchPre(TextureAtlas textureAtlas, Consumer<ResourceLocation> stitcher) {
		if (textureAtlas.location().equals(InventoryMenu.BLOCK_ATLAS)) {
			stitcher.accept(INPUT_ONLY_TEXTURE);
			stitcher.accept(TANK_TEXTURE);
			stitcher.accept(FE_ENERGY_EMPTY_TEXTURE);
			stitcher.accept(FE_ENERGY_FULL_TEXTURE);
			stitcher.accept(TR_ENERGY_EMPTY_TEXTURE);
			stitcher.accept(TR_ENERGY_FULL_TEXTURE);
		}
	}

	private void onTextureStitchPost(TextureAtlas textureAtlas) {
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
		BlockEntityRendererRegistry.register(FTBQuestsBlockEntities.CORE_TASK_SCREEN.get(), TaskScreenRenderer::new);
	}

	private void registerItemColors(Minecraft minecraft) {
		ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
			LootCrate crate = LootCrateItem.getCrate(stack);
			return crate == null ? 0xFFFFFFFF : (0xFF000000 | crate.color.rgb());
		}, FTBQuestsItems.LOOTCRATE.get());
	}

	private void onSidebarButtonCreated(SidebarButtonCreatedEvent event) {
		if (event.getButton().id.equals(QUESTS_BUTTON)) {
			event.getButton().setCustomTextHandler(() ->
			{
				if (ClientQuestFile.exists()) {
					if (ClientQuestFile.INSTANCE.disableGui && !ClientQuestFile.INSTANCE.canEdit()) {
						return "[X]";
					} else if (ClientQuestFile.INSTANCE.self.isLocked()) {
						return "[X]";
					} else if (ClientQuestFile.INSTANCE.self.hasUnclaimedRewards(Minecraft.getInstance().player.getUUID(), ClientQuestFile.INSTANCE)) {
						return "[!]";
					}
				}

				return "";
			});
		}
	}

	private void onFileCacheClear(QuestFile file) {
		if (!file.isServerSide()) {
			observationTasks = null;
		}
	}

	private void onKeyEvent(Minecraft mc) {
		if (FTBQuestsClient.KEY_QUESTS.consumeClick()) {
			ClientQuestFile.openGui();
		}
	}

	private EventResult onCustomClick(CustomClickEvent event) {
		if (event.id().getNamespace().equals(FTBQuests.MOD_ID) && "open_gui".equals(event.id().getPath())) {
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

			if (mc.hitResult != null && mc.hitResult.getType() != HitResult.Type.MISS) {
				for (ObservationTask task : observationTasks) {
					if (!ClientQuestFile.INSTANCE.self.isCompleted(task) && task.observe(mc.player, mc.hitResult) && ClientQuestFile.INSTANCE.self.canStartTasks(task.quest)) {
						currentlyObserving = task;
						break;
					}
				}
			}

			if (currentlyObserving != null) {
				if (!mc.isPaused()) {
					currentlyObservingTicks++;
				}

				if (currentlyObservingTicks >= currentlyObserving.timer) {
					new SubmitTaskMessage(currentlyObserving.id).sendToServer();
					ClientQuestFile.INSTANCE.self.addProgress(currentlyObserving, 1L);
					currentlyObserving = null;
					currentlyObservingTicks = 0L;
				}
			} else {
				currentlyObservingTicks = 0L;
			}
		}
	}

	private void onPlayerLogout(@Nullable LocalPlayer localPlayer) {
		StructureTask.syncKnownStructureList(List.of());
	}

	private void collectPinnedQuests(ClientQuestFile file) {
		TeamData data = file.self;

		List<Quest> pinnedQuests = new ArrayList<>();

		LongSet pinnedIds = data.getPinnedQuestIds(FTBQuests.PROXY.getClientPlayer());

		if (!pinnedIds.isEmpty()) {
			if (pinnedIds.contains(TeamData.AUTO_PIN_ID)) {
				// special auto-pin value: collect all quests which can be done now
				for (ChapterGroup group : file.chapterGroups) {
					for (Chapter chapter : group.chapters) {
						for (Quest quest : chapter.getQuests()) {
							if (!data.isCompleted(quest) && data.canStartTasks(quest)) {
								pinnedQuests.add(quest);
							}
						}
					}
				}
			} else {
				pinnedQuests = pinnedIds.longStream()
						.mapToObj(file::getQuest)
						.filter(Objects::nonNull)
						.toList();
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

			for (Task task : quest.tasks) {
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

	private void onScreenRender(PoseStack matrixStack, float tickDelta) {
		if (!ClientQuestFile.exists()) {
			return;
		}

		ClientQuestFile file = ClientQuestFile.INSTANCE;

		Minecraft mc = Minecraft.getInstance();
		int cy = mc.getWindow().getGuiScaledHeight() / 2;

		if (currentlyObserving != null) {
			int cx = mc.getWindow().getGuiScaledWidth() / 2;
			MutableComponent cot = currentlyObserving.getMutableTitle().withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE);
			int sw = mc.font.width(cot);
			int bw = Math.max(sw, 100);
			Color4I.DARK_GRAY.withAlpha(130).draw(matrixStack, cx - bw / 2 - 3, cy - 63, bw + 6, 29);
			GuiHelper.drawHollowRect(matrixStack, cx - bw / 2 - 3, cy - 63, bw + 6, 29, Color4I.DARK_GRAY, false);

			mc.font.drawShadow(matrixStack, cot, cx - sw / 2F, cy - 60, 0xFFFFFF);
			double completed = (currentlyObservingTicks + tickDelta) / (double) currentlyObserving.timer;

			GuiHelper.drawHollowRect(matrixStack, cx - bw / 2, cy - 49, bw, 12, Color4I.DARK_GRAY, false);
			Color4I.LIGHT_BLUE.withAlpha(130).draw(matrixStack, cx - bw / 2 + 1, cy - 48, (int) ((bw - 2D) * completed), 10);

			String cop = (currentlyObservingTicks * 100L / currentlyObserving.timer) + "%";
			mc.font.drawShadow(matrixStack, cop, cx - mc.font.width(cop) / 2F, cy - 47, 0xFFFFFF);
		}

		if (!pinnedQuestText.isEmpty()) {
			int width = 0;
			for (FormattedCharSequence s : pinnedQuestText) {
				width = Math.max(width, (int) mc.font.getSplitter().stringWidth(s));
			}

			float scale = ThemeProperties.PINNED_QUEST_SIZE.get(file).floatValue();

			matrixStack.pushPose();
			matrixStack.translate(mc.getWindow().getGuiScaledWidth() - width * scale - 8D, cy - pinnedQuestText.size() * 4.5D * scale, 100);
			matrixStack.scale(scale, scale, 1F);

			Color4I.BLACK.withAlpha(100).draw(matrixStack, 0, 0, width + 8, pinnedQuestText.size() * 9 + 8);

			for (int i = 0; i < pinnedQuestText.size(); i++) {
				mc.font.drawShadow(matrixStack, pinnedQuestText.get(i), 4, i * 9 + 4, 0xFFFFFFFF);
			}

			matrixStack.popPose();
		}
	}
}
