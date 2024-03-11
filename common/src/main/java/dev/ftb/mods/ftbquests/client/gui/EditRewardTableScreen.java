package dev.ftb.mods.ftbquests.client.gui;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.DoubleConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.config.ui.EditStringConfigOverlay;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class EditRewardTableScreen extends AbstractButtonListScreen {
	private final Runnable parentScreen;
	private final RewardTable editedTable;
	private final Consumer<RewardTable> callback;
	boolean changed = false;

	public EditRewardTableScreen(Runnable parentScreen, RewardTable originalTable, Consumer<RewardTable> callback) {
		this.parentScreen = parentScreen;
		this.callback = callback;

		editedTable = QuestObjectBase.copy(originalTable, () ->  new RewardTable(originalTable.id, originalTable.getFile()));

		setBorder(1, 1, 1);
	}

	@Override
	protected Panel createTopPanel() {
		return new CustomTopPanel();
	}

	@Override
	protected int getTopPanelHeight() {
		return 25;
	}

	@Override
	public void addButtons(Panel panel) {
		editedTable.getWeightedRewards().forEach(wr -> panel.add(new WeightedRewardButton(panel, wr)));
	}

	@Override
	public boolean onInit() {
		setTitle(Component.literal(Objects.requireNonNull(editedTable).getRawTitle()));

		return super.onInit();
	}

	@Override
	public Theme getTheme() {
		return NordTheme.THEME;
	}

	@Override
	public boolean onClosedByKey(Key key) {
		if (super.onClosedByKey(key)) {
			doCancel();
			return true;
		}

		return false;
	}

	@Override
	protected void doCancel() {
		if (changed) {
			openYesNo(Component.translatable("ftblibrary.unsaved_changes"), Component.empty(), parentScreen);
		} else {
			parentScreen.run();
		}
	}

	@Override
	protected void doAccept() {
		callback.accept(editedTable);
		parentScreen.run();
	}

	private class CustomTopPanel extends TopPanel {
		private final RewardTableSettingsButton settingsButton;
		private final AddWeightedRewardButton addButton;

		public CustomTopPanel() {
			settingsButton = new RewardTableSettingsButton(this);
			addButton = new AddWeightedRewardButton(this);
		}

		@Override
		public void addWidgets() {
			add(settingsButton);
			add(addButton);
		}

		@Override
		public void alignWidgets() {
			settingsButton.setPosAndSize(width - 18, 2, 16, 16);
			addButton.setPosAndSize(width - 36, 2, 16, 16);
		}

		@Override
		public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			super.draw(graphics, theme, x, y, w, h);

			editedTable.getIcon().draw(graphics, x + 2, y + 2, 16, 16);
			theme.drawString(graphics, getGui().getTitle(), x + 20, y + 6, Theme.SHADOW);
		}
	}

	private class RewardTableSettingsButton extends SimpleButton {
		private RewardTableSettingsButton(Panel panel) {
			super(panel, Component.translatable("gui.settings"), Icons.SETTINGS, (b, mb) -> {});
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			ConfigGroup group = new ConfigGroup(FTBQuestsAPI.MOD_ID, accepted -> run()) {
				@Override
				public Component getName() {
					return editedTable.getTitle();
				}
			};
			editedTable.fillConfigGroup(editedTable.createSubGroup(group));
			new EditConfigScreen(group).openGui();
		}
	}

	private class AddWeightedRewardButton extends SimpleButton {
		private AddWeightedRewardButton(Panel panel) {
			super(panel, Component.translatable("gui.add"), Icons.ADD, (b, mb) -> {});
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();

			List<ContextMenuItem> contextMenu = new ArrayList<>();
			for (RewardType type : RewardTypes.TYPES.values()) {
				if (!type.getExcludeFromListRewards()) {
					contextMenu.add(new ContextMenuItem(type.getDisplayName(), type.getIconSupplier(), b -> {
						playClickSound();
						type.getGuiProvider().openCreationGui(parent, editedTable.getFakeQuest(), reward -> {
							editedTable.addReward(new WeightedReward(reward, 1f));
							changed = true;
							openGui();
						});
					}));
				}
			}

			getGui().openContextMenu(contextMenu);
		}
	}

	private class WeightedRewardButton extends SimpleTextButton {
		private final WeightedReward wr;

		private WeightedRewardButton(Panel panel, WeightedReward wr) {
			super(panel, wr.getReward().getTitle(), wr.getReward().getIcon());
			this.wr = wr;
			setHeight(16);
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			super.addMouseOverText(list);

			if (getMouseX() > getX() + width - 13) {
				list.add(Component.translatable("gui.remove"));
			} else if (getMouseX() > getX() + width - 26) {
				list.add(Component.translatable("ftbquests.reward_table.set_weight"));
			} else {
				wr.getReward().addMouseOverText(list);
				String w = String.format("%.2f", wr.getWeight());
				String str = WeightedReward.chanceString(wr.getWeight(), editedTable.getTotalWeight(true));
				list.add(Component.translatable("ftbquests.reward_table.weight").append(": " + w)
						.append(Component.literal(" [" + str + "]").withStyle(ChatFormatting.DARK_GRAY)));
			}
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();

			if (button.isLeft()) {
				if (getMouseX() > getX() + width - 13) {
					openYesNo(Component.translatable("delete_item", wr.getReward().getTitle()), Component.empty(), this::doDeletion);
				} else if (getMouseX() > getX() + width - 26) {
					setEntryWeight();
				} else {
					editRewardTableEntry();
				}
			} else {
				List<ContextMenuItem> contextMenu = new ArrayList<>();
				contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.edit"), ItemIcon.getItemIcon(Items.FEATHER),
						b -> editRewardTableEntry()));
				contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.reward_table.set_weight"), ItemIcon.getItemIcon(Items.ANVIL),
						b -> setEntryWeight()));
				contextMenu.add(new ContextMenuItem(Component.translatable("gui.remove"), Icons.BIN, b -> doDeletion())
						.setYesNoText(Component.translatable("delete_item", wr.getReward().getTitle())));
				EditRewardTableScreen.this.openContextMenu(contextMenu);
			}
		}

		private void doDeletion() {
			editedTable.removeReward(wr);
			EditRewardTableScreen.this.refreshWidgets();
			changed = true;
		}

		private void setEntryWeight() {
			DoubleConfig c = new DoubleConfig(0D, Double.POSITIVE_INFINITY);
			c.setValue((double) wr.getWeight());
			EditStringConfigOverlay<Double> overlay = new EditStringConfigOverlay<>(parent, c, accepted -> {
				if (accepted) {
					wr.setWeight(c.getValue().floatValue());
					changed = true;
				}
			}).atPosition(parent.width - 80, getPosY());
			overlay.setExtraZlevel(300);
			getGui().pushModalPanel(overlay);
		}

		private void editRewardTableEntry() {
			ConfigGroup group = new ConfigGroup(FTBQuestsAPI.MOD_ID, accepted -> {
				if (accepted) {
					wr.getReward().clearCachedData();
					changed = true;
				}
				run();
			}) {
				@Override
				public Component getName() {
					return wr.getReward().getTitle();
				}
			};
			wr.getReward().fillConfigGroup(wr.getReward().createSubGroup(group));
			new EditConfigScreen(group).openGui();
		}

		@Override
		public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			if (isMouseOver) {
				Color4I.WHITE.withAlpha(30).draw(graphics, x, y, w, h);
				ItemIcon.getItemIcon(Items.ANVIL).draw(graphics, x + w - 26, y + 2, 12, 12);
				Icons.BIN.draw(graphics, x + w - 13, y + 2, 12, 12);
			}
			Color4I.GRAY.withAlpha(40).draw(graphics, x, y + h, w, 1);
		}

		@Override
		public Optional<PositionedIngredient> getIngredientUnderMouse() {
			return PositionedIngredient.of(wr.getReward().getIngredient(this), this);
		}
	}

}
