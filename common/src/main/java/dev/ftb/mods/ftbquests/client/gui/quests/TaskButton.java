package dev.ftb.mods.ftbquests.client.gui.quests;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.ButtonListBaseScreen;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.client.gui.ContextMenuBuilder;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.latvian.mods.itemfilters.api.IStringValueFilter;
import dev.latvian.mods.itemfilters.api.ItemFiltersAPI;
import dev.latvian.mods.itemfilters.api.ItemFiltersItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class TaskButton extends Button {
	private final QuestScreen questScreen;
	Task task;

	public TaskButton(Panel panel, Task task) {
		super(panel, task.getTitle(), Icons.ACCEPT);
		questScreen = (QuestScreen) panel.getGui();
		this.task = task;
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		if (isMouseOver()) {
			if (button.isRight() || getWidgetType() != WidgetType.DISABLED) {
				onClicked(button);
			}
			return true;
		}

		return false;
	}

	@Override
	public void onClicked(MouseButton button) {
		if (button.isLeft()) {
			boolean canClick = task.isValid()
					&& questScreen.file.selfTeamData.canStartTasks(task.getQuest())
					&& !questScreen.file.selfTeamData.isCompleted(task);
			task.onButtonClicked(this, canClick);
		} else if (button.isRight() && questScreen.file.canEdit()) {
			playClickSound();

			ContextMenuBuilder builder = ContextMenuBuilder.create(task, questScreen);

			if (task instanceof ItemTask itemTask) {
				var tags = itemTask.getItemStack().getItem().builtInRegistryHolder().tags().map(TagKey::location).toList();
				if (!tags.isEmpty() && !ItemFiltersAPI.isFilter(itemTask.getItemStack())) {
					builder.insertAtTop(List.of(new ContextMenuItem(Component.translatable("ftbquests.task.ftbquests.item.convert_tag"),
							ThemeProperties.RELOAD_ICON.get(),
							() -> {
								ItemStack tagFilter = new ItemStack(ItemFiltersItems.TAG.get());
								if (tags.size() == 1) {
									convertToSingleTag(itemTask, tags, tagFilter);
								} else {
									new TagSelectionScreen(tags, tagFilter, itemTask).openGui();
								}
							})
					));
				}
			}
			if (task.getIcon() instanceof ItemIcon itemIcon) {
				builder.insertAtTop(List.of(new ContextMenuItem(Component.translatable("ftbquests.gui.use_as_quest_icon"),
								ThemeProperties.EDIT_ICON.get(),
								() -> {
									task.getQuest().setRawIcon(itemIcon.getStack().copy());
									task.getQuest().clearCachedData();
									new EditObjectMessage(task.getQuest()).sendToServer();
								})
						)
				);
			}

			builder.openContextMenu(getGui());
		}
	}

	private static void convertToSingleTag(ItemTask itemTask, List<ResourceLocation> tags, ItemStack tagFilter) {
		String tag = tags.iterator().next().toString();
		((IStringValueFilter) tagFilter.getItem()).setValue(tagFilter, tag);
		itemTask.setStackAndCount(tagFilter, 1);

		if (itemTask.getRawTitle().isEmpty()) {
			itemTask.setRawTitle("Any #" + tag);
		}

		new EditObjectMessage(itemTask).sendToServer();
	}

	@Override
	public Optional<PositionedIngredient> getIngredientUnderMouse() {
		return task.getIngredient(this);
	}

	@Override
	public void addMouseOverText(TooltipList list) {
		questScreen.addInfoTooltip(list, task);

		task.addMouseOverHeader(list, questScreen.file.selfTeamData, Minecraft.getInstance().options.advancedItemTooltips);

		if (questScreen.file.selfTeamData.canStartTasks(task.getQuest())) {
			long maxp = task.getMaxProgress();
			long progress = questScreen.file.selfTeamData.getProgress(task);

			if (maxp > 1L) {
				if (task.hideProgressNumbers()) {
					list.add(Component.literal("[" + task.getRelativeProgressFromChildren(questScreen.file.selfTeamData) + "%]").withStyle(ChatFormatting.DARK_GREEN));
				} else {
					String max = isShiftKeyDown() ? Long.toUnsignedString(maxp) : task.formatMaxProgress();
					String prog = isShiftKeyDown() ? Long.toUnsignedString(progress) : task.formatProgress(questScreen.file.selfTeamData, progress);

					String s = (progress > maxp ? max : prog) + " / " + max;
					if (maxp < 100L) {
						list.add(Component.literal(s).withStyle(ChatFormatting.DARK_GREEN));
					} else {
						list.add(Component.literal(s).withStyle(ChatFormatting.DARK_GREEN).append(Component.literal(" [" + task.getRelativeProgressFromChildren(questScreen.file.selfTeamData) + "%]").withStyle(ChatFormatting.DARK_GRAY)));
					}

				}
			}
		}

		if (task.isOptionalForProgression()) {
			list.add(Component.translatable("ftbquests.quest.optional").withStyle(ChatFormatting.GRAY));
		}

		task.addMouseOverText(list, questScreen.file.selfTeamData);
	}

	@Override
	public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		if (isMouseOver()) {
			super.drawBackground(graphics, theme, x, y, w, h);
		}
	}

	@Override
	public void drawIcon(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		task.drawGUI(questScreen.file.selfTeamData, graphics, x, y, w, h);
	}

	@Override
	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		int bs = h >= 32 ? 32 : 16;
		GuiHelper.setupDrawing();
		drawBackground(graphics, theme, x, y, w, h);
		drawIcon(graphics, theme, x + (w - bs) / 2, y + (h - bs) / 2, bs, bs);

		if (questScreen.file.selfTeamData == null) {
			return;
		} else if (questScreen.getContextMenu().isPresent()) {
			//return;
		}

		PoseStack poseStack = graphics.pose();
		if (questScreen.file.selfTeamData.isCompleted(task)) {
			poseStack.pushPose();
			poseStack.translate(0, 0, 200);
			RenderSystem.enableBlend();
			ThemeProperties.CHECK_ICON.get().draw(graphics, x + w - 9, y + 1, 8, 8);
			poseStack.popPose();
		} else {
			MutableComponent s = task.getButtonText();

			if (s.getContents() != ComponentContents.EMPTY) {
				poseStack.pushPose();
				poseStack.translate(x + 19F - theme.getStringWidth(s) / 2F, y + 15F, 200F);
				poseStack.scale(0.5F, 0.5F, 1F);
				RenderSystem.enableBlend();
				theme.drawString(graphics, s, 0, 0, Color4I.WHITE, Theme.SHADOW);
				poseStack.popPose();
			}
		}
	}

	private class TagSelectionScreen extends ButtonListBaseScreen {
		private final List<ResourceLocation> tags;
		private final ItemStack tagFilter;
		private final ItemTask itemTask;

		public TagSelectionScreen(List<ResourceLocation> tags, ItemStack tagFilter, ItemTask itemTask) {
			this.tags = tags;
			this.tagFilter = tagFilter;
			this.itemTask = itemTask;
		}

		@Override
		public void addButtons(Panel panel) {
			for (ResourceLocation tag : tags) {
				panel.add(new SimpleTextButton(panel, Component.literal(tag.toString()), Color4I.empty()) {
					@Override
					public void onClicked(MouseButton button) {
						questScreen.openGui();
						((IStringValueFilter) tagFilter.getItem()).setValue(tagFilter, tag.toString());
						itemTask.setStackAndCount(tagFilter, 1);

						if (itemTask.getRawTitle().isEmpty()) {
							itemTask.setRawTitle("Any #" + tag);
						}

						new EditObjectMessage(itemTask).sendToServer();
					}
				});
			}
		}
	}
}
