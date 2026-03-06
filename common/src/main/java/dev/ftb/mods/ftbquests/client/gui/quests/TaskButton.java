package dev.ftb.mods.ftbquests.client.gui.quests;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftblibrary.client.gui.WidgetType;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.screens.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Button;
import dev.ftb.mods.ftblibrary.client.gui.widget.ContextMenuItem;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleTextButton;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.client.util.PositionedIngredient;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.api.ItemFilterAdapter;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.ContextMenuBuilder;
import dev.ftb.mods.ftbquests.integration.item_filtering.ItemMatchingSystem;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.net.GiveItemToPlayerMessage;
import dev.ftb.mods.ftbquests.net.ReorderItemMessage;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.CheckmarkTask;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.util.TextUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.joml.Matrix3x2fStack;

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
			if (task.getQuestFile().canEdit() && Minecraft.getInstance().hasAltDown()) {
				task.onEditButtonClicked(questScreen);
			} else {
				boolean canClick = task.isValid()
						&& FTBQuestsClient.getClientPlayerData().canStartTasks(task.getQuest())
						&& !FTBQuestsClient.getClientPlayerData().isCompleted(task);
				task.client().onButtonClicked(task, this, canClick);
			}
		} else if (button.isRight() && questScreen.file.canEdit()) {
			playClickSound();

			ContextMenuBuilder builder = ContextMenuBuilder.create(task, questScreen);

			builder.insertAtTop(List.of(new ContextMenuItem(Component.translatable("ftbquests.gui.move_left"), Icons.LEFT,
					b -> NetworkManager.sendToServer(new ReorderItemMessage(task.getId(), false))
			)));
			builder.insertAtTop(List.of(new ContextMenuItem(Component.translatable("ftbquests.gui.move_right"), Icons.RIGHT,
					b -> NetworkManager.sendToServer(new ReorderItemMessage(task.getId(), true))
			)));

			if (task instanceof ItemTask itemTask && !itemTask.getItemStack().isEmpty()) {
				var tags =  itemTask.getItemStack().getItem().builtInRegistryHolder().tags().toList();
				if (!tags.isEmpty() && !ItemMatchingSystem.INSTANCE.isItemFilter(itemTask.getItemStack())) {
					for (ItemFilterAdapter adapter : ItemMatchingSystem.INSTANCE.adapters()) {
						if (adapter.hasItemTagFilter()) {
							builder.insertAtTop(List.of(new ContextMenuItem(Component.translatable("ftbquests.task.ftbquests.item.convert_tag", adapter.getName()),
									ThemeProperties.RELOAD_ICON.get(),
									b -> {
										if (tags.size() == 1) {
											setTagFilterAndSave(itemTask, adapter, tags.getFirst());
										} else {
											new TagSelectionScreen(tags, itemTask, adapter).openGui();
										}
									})
							));
						}
					}
				}
				builder.insertAtTop(List.of(new ContextMenuItem(Component.translatable("ftbquests.task.grab_item"), Icons.ADD_GRAY,
						b -> NetworkManager.sendToServer(new GiveItemToPlayerMessage(itemTask.getItemStack()))))
				);
			}
			if (task.getIcon() instanceof ItemIcon itemIcon) {
				builder.insertAtTop(List.of(new ContextMenuItem(Component.translatable("ftbquests.gui.use_as_quest_icon"),
								ThemeProperties.EDIT_ICON.get(),
								b -> {
									task.getQuest().setRawIcon(itemIcon.getStack().copy());
									task.getQuest().clearCachedData();
									EditObjectMessage.sendToServer(task.getQuest());
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

		EditObjectMessage.sendToServer(itemTask);
	}

	@Override
	public Optional<PositionedIngredient> getIngredientUnderMouse() {
		return task.getIngredient(this);
	}

	@Override
	public void addMouseOverText(TooltipList list) {
		questScreen.addInfoTooltip(list, task);

		TeamData teamData = FTBQuestsClient.getClientPlayerData();

		task.addMouseOverHeader(list, teamData, Minecraft.getInstance().options.advancedItemTooltips);

		if (teamData.canStartTasks(task.getQuest())) {
			long maxp = task.getMaxProgress();
			long progress = teamData.getProgress(task);

			if (maxp > 1L) {
				if (task.hideProgressNumbers()) {
					list.add(Component.literal("[" + task.getRelativeProgressFromChildren(teamData) + "%]").withStyle(ChatFormatting.DARK_GREEN));
				} else {
					String max = isShiftKeyDown() ? Long.toUnsignedString(maxp) : task.formatMaxProgress();
					String prog = isShiftKeyDown() ? Long.toUnsignedString(progress) : task.formatProgress(teamData, progress);

					String s = (progress > maxp ? max : prog) + " / " + max;
					if (maxp < 100L) {
						list.add(Component.literal(s).withStyle(ChatFormatting.DARK_GREEN));
					} else {
						list.add(Component.literal(s).withStyle(ChatFormatting.DARK_GREEN).append(Component.literal(" [" + task.getRelativeProgressFromChildren(teamData) + "%]").withStyle(ChatFormatting.DARK_GRAY)));
					}
				}
			}
		}

		if (task.isOptionalForProgression(teamData)) {
			list.add(Component.translatable("ftbquests.quest.misc.optional_task").withStyle(ChatFormatting.GRAY));
		}

		task.addMouseOverText(list, teamData);
	}

	@Override
	public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		if (isMouseOver()) {
			super.drawBackground(graphics, theme, x, y, w, h);
		}
	}

	@Override
	public void drawIcon(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		if (task instanceof CheckmarkTask) {
			TeamData teamData = FTBQuestsClient.getClientPlayerData();

			var icon = ThemeProperties.CHECKMARK_TASK_INACTIVE.get(task);
			if (teamData.isCompleted(task)) {
				icon = ThemeProperties.CHECKMARK_TASK_ACTIVE.get(task);
			}

			IconHelper.renderIcon(icon, graphics, x, y, w, h);
			return;
		}

		IconHelper.renderIcon(task.getIcon(), graphics, x, y, w, h);
	}

	@Override
	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		int bs = h >= 32 ? 32 : 16;
		drawBackground(graphics, theme, x, y, w, h);
		drawIcon(graphics, theme, x + (w - bs) / 2, y + (h - bs) / 2, bs, bs);

		Matrix3x2fStack poseStack = graphics.pose();
		if (FTBQuestsClient.getClientPlayerData().isCompleted(task)) {
			poseStack.pushMatrix();
			poseStack.translate(0, 0);
			IconHelper.renderIcon(ThemeProperties.CHECK_ICON.get(), graphics, x + w - 9, y + 1, 8, 8);
			poseStack.popMatrix();
		} else {
			MutableComponent buttonText = task.getButtonText();
			if (!TextUtils.isComponentEmpty(buttonText)) {
				poseStack.pushMatrix();
				poseStack.translate(x + 19F - theme.getStringWidth(buttonText) / 2F, y + 15F);
				poseStack.scale(0.5F, 0.5F);
				theme.drawString(graphics, buttonText, 0, 0, Color4I.WHITE, Theme.SHADOW);
				poseStack.popMatrix();
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
			setSize(Math.max(titleW, w) + 20, getWindow().getGuiScaledHeight() * 3 / 4);

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
					IconHelper.renderIcon(Color4I.WHITE.withAlpha(30), graphics, x, y, w, h);
				}
				IconHelper.renderIcon(Color4I.GRAY.withAlpha(40), graphics, x, y + h, w, 1);
			}
		}
	}
}
