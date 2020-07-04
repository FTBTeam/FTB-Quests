package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.events.SidebarButtonCreatedEvent;
import com.feed_the_beast.ftblib.events.client.CustomClickEvent;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.block.BlockDetector;
import com.feed_the_beast.ftbquests.events.ClearFileCacheEvent;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.item.ItemLootCrate;
import com.feed_the_beast.ftbquests.net.MessageSubmitTask;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.task.ObservationTask;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.ftbquests.tile.TilePlayerDetector;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenCore;
import com.feed_the_beast.ftbquests.tile.TileRewardCollector;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenCore;
import com.feed_the_beast.ftbquests.util.RayMatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBQuests.MOD_ID, value = Side.CLIENT)
public class FTBQuestsClientEventHandler
{
	private static final ResourceLocation QUESTS_BUTTON = new ResourceLocation(FTBQuests.MOD_ID, "quests");

	public static TextureAtlasSprite inputBlockSprite;
	private static List<ObservationTask> observationTasks = null;

	private static ObservationTask currentlyObserving = null;
	private static int currentlyObservingTime = 0;

	private static void addModel(Item item, String variant)
	{
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), variant));
	}

	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event)
	{
		addModel(FTBQuestsItems.SCREEN, "facing=north");
		addModel(FTBQuestsItems.PROGRESS_DETECTOR, "normal");

		for (BlockDetector.Variant variant : BlockDetector.Variant.VALUES)
		{
			ModelLoader.setCustomModelResourceLocation(FTBQuestsItems.DETECTOR, variant.ordinal(), new ModelResourceLocation(FTBQuestsItems.DETECTOR.getRegistryName(), "variant=" + variant.getName()));
		}

		addModel(FTBQuestsItems.PROGRESS_SCREEN, "facing=north");
		addModel(FTBQuestsItems.CHEST, "facing=north");
		addModel(FTBQuestsItems.LOOT_CRATE_STORAGE, "normal");
		addModel(FTBQuestsItems.LOOT_CRATE_OPENER, "normal");
		addModel(FTBQuestsItems.BARRIER, "completed=false");
		addModel(FTBQuestsItems.REWARD_COLLECTOR, "normal");

		addModel(FTBQuestsItems.BOOK, "inventory");
		addModel(FTBQuestsItems.LOOTCRATE, "inventory");
		addModel(FTBQuestsItems.CUSTOM_ICON, "inventory");

		ClientRegistry.bindTileEntitySpecialRenderer(TileTaskScreenCore.class, new RenderTaskScreen());
		ClientRegistry.bindTileEntitySpecialRenderer(TileProgressScreenCore.class, new RenderProgressScreen());
		ClientRegistry.bindTileEntitySpecialRenderer(TilePlayerDetector.class, new RenderPlayerDetector());
		ClientRegistry.bindTileEntitySpecialRenderer(TileRewardCollector.class, new RenderRewardCollector());
	}

	@SubscribeEvent
	public static void registerItemColors(ColorHandlerEvent.Item event)
	{
		event.getItemColors().registerItemColorHandler((stack, tintIndex) -> {
			LootCrate crate = ItemLootCrate.getCrate(null, stack);
			return crate == null ? 0xFFFFFFFF : (0xFF000000 | crate.color.rgb());
		}, FTBQuestsItems.LOOTCRATE);
	}

	@SubscribeEvent
	public static void onSidebarButtonCreated(SidebarButtonCreatedEvent event)
	{
		if (event.getButton().id.equals(QUESTS_BUTTON))
		{
			event.getButton().setCustomTextHandler(() ->
			{
				if (ClientQuestFile.exists())
				{
					if (!ClientQuestFile.existsWithTeam())
					{
						return "[!]";
					}

					int r = ClientQuestFile.INSTANCE.getUnclaimedRewards(Minecraft.getMinecraft().player.getUniqueID(), ClientQuestFile.INSTANCE.self, true);

					if (r > 0)
					{
						return Integer.toString(r);
					}
				}

				return "";
			});

			event.getButton().setTooltipHandler(list -> {
				if (ClientQuestFile.exists() && !ClientQuestFile.existsWithTeam())
				{
					list.add(TextFormatting.GRAY + I18n.format("sidebar_button.ftbquests.quests.no_team"));
				}
			});
		}
	}

	@SubscribeEvent
	public static void onFileCacheClear(ClearFileCacheEvent event)
	{
		observationTasks = null;
	}

	@SubscribeEvent
	public static void onKeyEvent(InputEvent.KeyInputEvent event)
	{
		if (FTBQuestsClient.KEY_QUESTS.isPressed())
		{
			ClientQuestFile.INSTANCE.openQuestGui(Minecraft.getMinecraft().player);
		}
	}

	@SubscribeEvent
	public static void onCustomClick(CustomClickEvent event)
	{
		if (event.getID().getNamespace().equals(FTBQuests.MOD_ID) && "open_gui".equals(event.getID().getPath()))
		{
			ClientQuestFile.INSTANCE.openQuestGui(Minecraft.getMinecraft().player);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onTextureStitchPre(TextureStitchEvent.Pre event)
	{
		inputBlockSprite = event.getMap().registerSprite(new ResourceLocation(FTBQuests.MOD_ID, "blocks/screen_front_input"));
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event)
	{
		Minecraft mc = Minecraft.getMinecraft();

		if (event.phase == TickEvent.Phase.START && mc.world != null && ClientQuestFile.existsWithTeam())
		{
			if (observationTasks == null)
			{
				observationTasks = ClientQuestFile.INSTANCE.collect(ObservationTask.class);
			}

			if (observationTasks.isEmpty())
			{
				return;
			}

			currentlyObserving = null;

			RayMatcher.Data data = RayMatcher.Data.get(mc.world, mc.objectMouseOver);

			for (ObservationTask task : observationTasks)
			{
				if (!task.isComplete(ClientQuestFile.INSTANCE.self) && task.matcher.matches(data) && task.quest.canStartTasks(ClientQuestFile.INSTANCE.self))
				{
					currentlyObserving = task;
					break;
				}
			}

			if (currentlyObserving != null)
			{
				if (!mc.isGamePaused())
				{
					currentlyObservingTime++;
				}

				if (currentlyObservingTime >= currentlyObserving.timer.ticks())
				{
					new MessageSubmitTask(currentlyObserving.id).sendToServer();
					ClientQuestFile.INSTANCE.self.getTaskData(currentlyObserving).addProgress(1L);
					currentlyObserving = null;
					currentlyObservingTime = 0;
				}
			}
			else
			{
				currentlyObservingTime = 0;
			}
		}
	}

	@SubscribeEvent
	public static void onScreenRender(RenderGameOverlayEvent.Post event)
	{
		if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || !ClientQuestFile.existsWithTeam())
		{
			return;
		}

		GlStateManager.enableBlend();
		Minecraft mc = Minecraft.getMinecraft();
		int cy = event.getResolution().getScaledHeight() / 2;

		if (currentlyObserving != null)
		{
			int cx = event.getResolution().getScaledWidth() / 2;
			String cot = TextFormatting.UNDERLINE.toString() + TextFormatting.YELLOW + currentlyObserving.getTitle();
			int sw = mc.fontRenderer.getStringWidth(cot);
			int bw = Math.max(sw, 100);
			Color4I.DARK_GRAY.withAlpha(130).draw(cx - bw / 2 - 3, cy - 63, bw + 6, 29);
			GuiHelper.drawHollowRect(cx - bw / 2 - 3, cy - 63, bw + 6, 29, Color4I.DARK_GRAY, false);

			mc.fontRenderer.drawStringWithShadow(cot, cx - sw / 2, cy - 60, 0xFFFFFF);
			double completed = (currentlyObservingTime + event.getPartialTicks()) / (double) currentlyObserving.timer.ticks();

			GuiHelper.drawHollowRect(cx - bw / 2, cy - 49, bw, 12, Color4I.DARK_GRAY, false);
			Color4I.LIGHT_BLUE.withAlpha(130).draw(cx - bw / 2 + 1, cy - 48, (int) ((bw - 2D) * completed), 10);

			String cop = (currentlyObservingTime * 100L / currentlyObserving.timer.ticks()) + "%";
			mc.fontRenderer.drawStringWithShadow(cop, cx - mc.fontRenderer.getStringWidth(cop) / 2, cy - 47, 0xFFFFFF);
		}

		if (!ClientQuestFile.INSTANCE.pinnedQuests.isEmpty())
		{
			List<String> list = new ArrayList<>();
			boolean first = true;

			if (ClientQuestFile.INSTANCE.pinnedQuests.contains(1))
			{
				for (Chapter chapter : ClientQuestFile.INSTANCE.chapters)
				{
					for (Quest quest : chapter.quests)
					{
						if (!quest.isComplete(ClientQuestFile.INSTANCE.self) && quest.canStartTasks(ClientQuestFile.INSTANCE.self))
						{
							if (first)
							{
								first = false;
							}
							else
							{
								list.add("");
							}

							list.add(TextFormatting.BOLD + mc.fontRenderer.trimStringToWidth(quest.getTitle(), 160) + " " + TextFormatting.DARK_AQUA + quest.getRelativeProgress(ClientQuestFile.INSTANCE.self) + "%");

							for (Task task : quest.tasks)
							{
								if (!task.isComplete(ClientQuestFile.INSTANCE.self))
								{
									list.add(TextFormatting.GRAY + mc.fontRenderer.trimStringToWidth(task.getTitle(), 160) + " " + TextFormatting.GREEN + ClientQuestFile.INSTANCE.self.getTaskData(task).getProgressString() + "/" + task.getMaxProgressString());
								}
							}
						}
					}
				}
			}
			else
			{
				for (int q : ClientQuestFile.INSTANCE.pinnedQuests)
				{
					Quest quest = ClientQuestFile.INSTANCE.getQuest(q);

					if (quest != null)
					{
						if (first)
						{
							first = false;
						}
						else
						{
							list.add("");
						}

						if (quest.isComplete(ClientQuestFile.INSTANCE.self))
						{
							list.add(TextFormatting.BOLD.toString() + TextFormatting.GREEN + mc.fontRenderer.trimStringToWidth(quest.getTitle(), 160) + TextFormatting.DARK_GREEN + " 100%");
						}
						else
						{
							list.add(TextFormatting.BOLD + mc.fontRenderer.trimStringToWidth(quest.getTitle(), 160) + " " + TextFormatting.DARK_AQUA + quest.getRelativeProgress(ClientQuestFile.INSTANCE.self) + "%");

							for (Task task : quest.tasks)
							{
								if (!task.isComplete(ClientQuestFile.INSTANCE.self))
								{
									list.add(TextFormatting.GRAY + mc.fontRenderer.trimStringToWidth(task.getTitle(), 160) + " " + TextFormatting.GREEN + ClientQuestFile.INSTANCE.self.getTaskData(task).getProgressString() + "/" + task.getMaxProgressString());
								}
							}
						}
					}
				}
			}

			if (!list.isEmpty())
			{
				int mw = 0;

				for (String s : list)
				{
					mw = Math.max(mw, mc.fontRenderer.getStringWidth(s));
				}

				double scale = ThemeProperties.PINNED_QUEST_SIZE.get(ClientQuestFile.INSTANCE);

				GlStateManager.pushMatrix();
				GlStateManager.translate(event.getResolution().getScaledWidth() - mw * scale - 8D, cy - list.size() * 4.5D * scale, 100D);
				GlStateManager.scale(scale, scale, 1D);

				Color4I.BLACK.withAlpha(100).draw(0, 0, mw + 8, list.size() * 9 + 8);

				for (int i = 0; i < list.size(); i++)
				{
					mc.fontRenderer.drawStringWithShadow(list.get(i), 4, i * 9 + 4, 0xFFFFFFFF);
				}

				GlStateManager.popMatrix();
			}
		}
	}
}