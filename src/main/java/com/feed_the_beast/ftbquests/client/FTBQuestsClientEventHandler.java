package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.events.ClearFileCacheEvent;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.item.ItemLootCrate;
import com.feed_the_beast.ftbquests.net.MessageSubmitTask;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.task.ObservationTask;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.sidebar.SidebarButtonCreatedEvent;
import com.feed_the_beast.mods.ftbguilibrary.widget.CustomClickEvent;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class FTBQuestsClientEventHandler
{
	private static final ResourceLocation QUESTS_BUTTON = new ResourceLocation(FTBQuests.MOD_ID, "quests");

	private List<ObservationTask> observationTasks = null;
	private ObservationTask currentlyObserving = null;
	private long currentlyObservingTicks = 0L;

	public void init()
	{
		MinecraftForge.EVENT_BUS.addListener(this::registerItemColors);
		MinecraftForge.EVENT_BUS.addListener(this::onSidebarButtonCreated);
		MinecraftForge.EVENT_BUS.addListener(this::onFileCacheClear);
		MinecraftForge.EVENT_BUS.addListener(this::onKeyEvent);
		MinecraftForge.EVENT_BUS.addListener(this::onCustomClick);
		MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
		MinecraftForge.EVENT_BUS.addListener(this::onScreenRender);
	}

	private void registerItemColors(ColorHandlerEvent.Item event)
	{
		event.getItemColors().register((stack, tintIndex) -> {
			LootCrate crate = ItemLootCrate.getCrate(null, stack);
			return crate == null ? 0xFFFFFFFF : (0xFF000000 | crate.color.rgb());
		}, FTBQuestsItems.LOOTCRATE);
	}

	private void onSidebarButtonCreated(SidebarButtonCreatedEvent event)
	{
		if (event.getButton().id.equals(QUESTS_BUTTON))
		{
			event.getButton().setCustomTextHandler(() ->
			{
				if (ClientQuestFile.exists() && ClientQuestFile.INSTANCE.self.hasUnclaimedRewards())
				{
					return "[!]";
				}

				return "";
			});
		}
	}

	private void onFileCacheClear(ClearFileCacheEvent event)
	{
		observationTasks = null;
	}

	private void onKeyEvent(InputEvent.KeyInputEvent event)
	{
		if (FTBQuestsClient.KEY_QUESTS.consumeClick())
		{
			ClientQuestFile.INSTANCE.openQuestGui();
		}
	}

	private void onCustomClick(CustomClickEvent event)
	{
		if (event.getId().getNamespace().equals(FTBQuests.MOD_ID) && "open_gui".equals(event.getId().getPath()))
		{
			if (!ClientQuestFile.exists())
			{
				Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.TUTORIAL_HINT, new TextComponent("Error?! Server doesn't have FTB Quests!"), null));
			}
			else
			{
				ClientQuestFile.INSTANCE.openQuestGui();
			}

			event.setCanceled(true);
		}
	}

	private void onClientTick(TickEvent.ClientTickEvent event)
	{
		Minecraft mc = Minecraft.getInstance();

		if (event.phase == TickEvent.Phase.START && mc.level != null && ClientQuestFile.exists())
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

			for (ObservationTask task : observationTasks)
			{
				if (!ClientQuestFile.INSTANCE.self.isComplete(task) && task.matcher.check(mc.player, mc.hitResult) && ClientQuestFile.INSTANCE.self.canStartTasks(task.quest))
				{
					currentlyObserving = task;
					break;
				}
			}

			if (currentlyObserving != null)
			{
				if (!mc.isPaused())
				{
					currentlyObservingTicks++;
				}

				if (currentlyObservingTicks >= currentlyObserving.ticks)
				{
					new MessageSubmitTask(currentlyObserving.id).sendToServer();
					ClientQuestFile.INSTANCE.self.getTaskData(currentlyObserving).addProgress(1L);
					currentlyObserving = null;
					currentlyObservingTicks = 0L;
				}
			}
			else
			{
				currentlyObservingTicks = 0L;
			}
		}
	}

	private void onScreenRender(RenderGameOverlayEvent.Post event)
	{
		if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || !ClientQuestFile.exists())
		{
			return;
		}

		ClientQuestFile file = ClientQuestFile.INSTANCE;
		PlayerData data = file.self;

		GlStateManager._enableBlend();
		Minecraft mc = Minecraft.getInstance();
		PoseStack matrixStack = event.getMatrixStack();
		int cy = event.getWindow().getGuiScaledHeight() / 2;

		if (currentlyObserving != null)
		{
			int cx = event.getWindow().getGuiScaledWidth() / 2;
			MutableComponent cot = currentlyObserving.getTitle().copy().withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE);
			int sw = mc.font.width(cot);
			int bw = Math.max(sw, 100);
			Color4I.DARK_GRAY.withAlpha(130).draw(matrixStack, cx - bw / 2 - 3, cy - 63, bw + 6, 29);
			GuiHelper.drawHollowRect(matrixStack, cx - bw / 2 - 3, cy - 63, bw + 6, 29, Color4I.DARK_GRAY, false);

			mc.font.drawShadow(matrixStack, cot, cx - sw / 2F, cy - 60, 0xFFFFFF);
			double completed = (currentlyObservingTicks + event.getPartialTicks()) / (double) currentlyObserving.ticks;

			GuiHelper.drawHollowRect(matrixStack, cx - bw / 2, cy - 49, bw, 12, Color4I.DARK_GRAY, false);
			Color4I.LIGHT_BLUE.withAlpha(130).draw(matrixStack, cx - bw / 2 + 1, cy - 48, (int) ((bw - 2D) * completed), 10);

			String cop = (currentlyObservingTicks * 100L / currentlyObserving.ticks) + "%";
			mc.font.drawShadow(matrixStack, cop, cx - mc.font.width(cop) / 2F, cy - 47, 0xFFFFFF);
		}

		if (!data.pinnedQuests.isEmpty())
		{
			List<FormattedCharSequence> list = new ArrayList<>();
			boolean first = true;

			if (data.pinnedQuests.contains(1))
			{
				for (Chapter chapter : file.chapters)
				{
					for (Quest quest : chapter.quests)
					{
						if (!data.isComplete(quest) && data.canStartTasks(quest))
						{
							if (first)
							{
								first = false;
							}
							else
							{
								list.add(FormattedCharSequence.EMPTY);
							}

							/* FIXME
							list.add(TextFormatting.BOLD + mc.fontRenderer.trimStringToWidth(quest.getTitle(), 160) + " " + TextFormatting.DARK_AQUA + data.getRelativeProgress(quest) + "%");

							for (Task task : quest.tasks)
							{
								if (!data.isComplete(task))
								{
									list.add(TextFormatting.GRAY + mc.fontRenderer.trimStringToWidth(task.getTitle(), 160) + " " + TextFormatting.GREEN + data.getTaskData(task).getProgressString() + "/" + task.getMaxProgressString());
								}
							}
							*/
						}
					}
				}
			}
			else
			{
				for (int q : data.pinnedQuests)
				{
					Quest quest = file.getQuest(q);

					if (quest != null)
					{
						if (first)
						{
							first = false;
						}
						else
						{
							list.add(FormattedCharSequence.EMPTY);
						}

						if (data.isComplete(quest))
						{
							TextComponent component = new TextComponent("");
							component.append(quest.getTitle().withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN));
							component.append(new TextComponent(" 100%").withStyle(ChatFormatting.DARK_GREEN));
							list.addAll(mc.font.split(component, 160));
						}
						else
						{
							/* FIXME
							list.add(TextFormatting.BOLD + mc.fontRenderer.trimStringToWidth(quest.getTitle(), 160) + " " + TextFormatting.DARK_AQUA + data.getRelativeProgress(quest) + "%");

							for (Task task : quest.tasks)
							{
								if (!data.isComplete(task))
								{
									list.add(TextFormatting.GRAY + mc.fontRenderer.trimStringToWidth(task.getTitle(), 160) + " " + TextFormatting.GREEN + data.getTaskData(task).getProgressString() + "/" + task.getMaxProgressString());
								}
							}
							*/
						}
					}
				}
			}

			if (!list.isEmpty())
			{
				int mw = 0;

				for (FormattedCharSequence s : list)
				{
					mw = Math.max(mw, (int) mc.font.getSplitter().stringWidth(s));
				}

				float scale = ThemeProperties.PINNED_QUEST_SIZE.get(file).floatValue();

				matrixStack.pushPose();
				matrixStack.translate(event.getWindow().getGuiScaledWidth() - mw * scale - 8D, cy - list.size() * 4.5D * scale, 100D);
				matrixStack.scale(scale, scale, 1F);

				Color4I.BLACK.withAlpha(100).draw(matrixStack, 0, 0, mw + 8, list.size() * 9 + 8);

				for (int i = 0; i < list.size(); i++)
				{
					mc.font.drawShadow(matrixStack, list.get(i), 4, i * 9 + 4, 0xFFFFFFFF);
				}

				matrixStack.popPose();
			}
		}
	}
}