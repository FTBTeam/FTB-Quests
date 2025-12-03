package dev.ftb.mods.ftbquests.client.gui.quests;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.api.ItemFilterAdapter;
import dev.ftb.mods.ftbquests.client.gui.ContextMenuBuilder;
import dev.ftb.mods.ftbquests.integration.item_filtering.ItemMatchingSystem;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.net.GiveItemToPlayerMessage;
import dev.ftb.mods.ftbquests.net.ReorderItemMessage;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Comparator;
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
			if (task.getQuestFile().canEdit() && ScreenWrapper.hasAltDown()) {
				task.onEditButtonClicked(questScreen);
			} else {
				boolean canClick = task.isValid()
						&& questScreen.file.selfTeamData.canStartTasks(task.getQuest())
						&& !questScreen.file.selfTeamData.isCompleted(task);
				task.onButtonClicked(this, canClick);
			}
		} else if (button.isRight() && questScreen.file.canEdit()) {
			playClickSound();

			ContextMenuBuilder builder = ContextMenuBuilder.create(task, questScreen);

			builder.insertAtTop(List.of(new ContextMenuItem(Component.translatable("ftbquests.gui.move_left"), Icons.LEFT,
					b -> new ReorderItemMessage(task.getId(), false).sendToServer()
			)));
			builder.insertAtTop(List.of(new ContextMenuItem(Component.translatable("ftbquests.gui.move_right"), Icons.RIGHT,
					b -> new ReorderItemMessage(task.getId(), true).sendToServer()
			)));

			if (task instanceof ItemTask itemTask && !itemTask.getItemStack().isEmpty()) {
				var tags = itemTask.getItemStack().getItem().builtInRegistryHolder().tags().toList();
				if (!tags.isEmpty() && !ItemMatchingSystem.INSTANCE.isItemFilter(itemTask.getItemStack())) {
					for (ItemFilterAdapter adapter : ItemMatchingSystem.INSTANCE.adapters()) {
						if (adapter.hasItemTagFilter()) {
							builder.insertAtTop(List.of(new ContextMenuItem(Component.translatable("ftbquests.task.ftbquests.item.convert_tag", adapter.getName()),
									ThemeProperties.RELOAD_ICON.get(),
									b -> {
										if (tags.size() == 1) {
											setTagFilterAndSave(itemTask, adapter, tags.get(0));
										} else {
											new TagSelectionScreen(tags, itemTask, adapter).openGui();
										}
									})
							));
						}
					}
				}
				builder.insertAtTop(List.of(new ContextMenuItem(Component.translatable("ftbquests.task.grab_item"), Icons.ADD_GRAY,
						b -> new GiveItemToPlayerMessage(itemTask.getItemStack()).sendToServer()))
				);
			}
			if (task.getIcon() instanceof ItemIcon itemIcon) {
				builder.insertAtTop(List.of(new ContextMenuItem(Component.translatable("ftbquests.gui.use_as_quest_icon"),
								ThemeProperties.EDIT_ICON.get(),
								b -> {
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

	private void setTagFilterAndSave(ItemTask itemTask, ItemFilterAdapter adapter, TagKey<Item> tag) {
		itemTask.setStackAndCount(adapter.makeTagFilterStack(tag), itemTask.getItemStack().getCount());

		if (itemTask.getRawTitle().isEmpty()) {
			itemTask.setRawTitle("Any #" + tag.location());
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
			list.add(Component.translatable("ftbquests.quest.misc.optional_task").withStyle(ChatFormatting.GRAY));
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

	private class TagSelectionScreen extends AbstractButtonListScreen {
		private final List<TagKey<Item>> tags;
		private final ItemTask itemTask;
		private final ItemFilterAdapter adapter;

		public TagSelectionScreen(List<TagKey<Item>> tags, ItemTask itemTask, ItemFilterAdapter adapter) {
			this.itemTask = itemTask;
			this.tags = tags;
			this.adapter = adapter;

			setTitle(Component.translatable("ftbquests.task.ftbquests.item.select_tag"));
			showBottomPanel(false);
			showCloseButton(true);
		}

		@Override
		public void addButtons(Panel panel) {
			tags.stream()
					.sorted(Comparator.comparing(itemTagKey -> itemTagKey.location().toString()))
					.forEach(tag -> panel.add(new TagSelectionButton(panel, tag)));
		}

		@Override
		public boolean onInit() {
			int titleW = getTheme().getStringWidth(getTitle());
			int w = tags.stream()
					.map(t -> getTheme().getStringWidth(t.location().toString()))
					.max(Comparator.naturalOrder())
					.orElse(100);
			setSize(Math.max(titleW, w) + 20, getScreen().getGuiScaledHeight() * 3 / 4);

			return true;
		}

		@Override
		protected void doCancel() {
			questScreen.openGui();
		}

		@Override
		protected void doAccept() {
			questScreen.openGui();
		}

		private class TagSelectionButton extends SimpleTextButton {
			private final TagKey<Item> tag;

			public TagSelectionButton(Panel panel, TagKey<Item> tag) {
				super(panel, Component.literal(tag.location().toString()), Color4I.empty());
				this.tag = tag;
			}

			@Override
			public void onClicked(MouseButton button) {
				questScreen.openGui();
				setTagFilterAndSave(itemTask, adapter, tag);
			}

			@Override
			public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
				if (isMouseOver) {
					Color4I.WHITE.withAlpha(30).draw(graphics, x, y, w, h);
				}
				Color4I.GRAY.withAlpha(40).draw(graphics, x, y + h, w, 1);
			}
		}
	}
}
