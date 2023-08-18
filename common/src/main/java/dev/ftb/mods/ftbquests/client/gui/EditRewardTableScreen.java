package dev.ftb.mods.ftbquests.client.gui;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.DoubleConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigFromStringScreen;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.ButtonListBaseScreen;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EditRewardTableScreen extends ButtonListBaseScreen {
	private final RewardTable originalTable;
	private final RewardTable rewardTable;
	private final Runnable callback;

	public EditRewardTableScreen(RewardTable originalTable, Runnable callback) {
		this.originalTable = originalTable;
		this.callback = callback;

		rewardTable = QuestObjectBase.copy(originalTable, () ->  new RewardTable(0L, originalTable.getFile()));

		setTitle(Component.translatable("ftbquests.reward_table").append(": " + Objects.requireNonNull(rewardTable).getRawTitle()));
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel) {
		panel.add(new RewardTableSettingsButton(panel));
		panel.add(new SaveRewardTableButton(panel));
		panel.add(new AddWeightedRewardButton(panel));
		panel.add(new VerticalSpaceWidget(panel, 1));

		rewardTable.getWeightedRewards().forEach(wr -> panel.add(new WeightedRewardButton(panel, wr)));
	}

	@Override
	public void alignWidgets() {
		// bit of a kludge, but stops the screen getting a bit narrower each time it's re-opened after adding/deleting an entry
		this.width = 186;
		super.alignWidgets();
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}

	@Override
	public boolean keyPressed(Key key) {
		if (key.esc()) {
			onBack();
			return true;
		} else {
			return super.keyPressed(key);
		}
	}

	private class RewardTableSettingsButton extends SimpleTextButton {
		private RewardTableSettingsButton(Panel panel) {
			super(panel, Component.translatable("gui.settings"), Icons.SETTINGS);
			setHeight(12);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			ConfigGroup group = new ConfigGroup(FTBQuestsAPI.MOD_ID, accepted -> run());
			rewardTable.fillConfigGroup(rewardTable.createSubGroup(group));
			new EditConfigScreen(group).openGui();
		}
	}

	private class SaveRewardTableButton extends SimpleTextButton {
		private SaveRewardTableButton(Panel panel) {
			super(panel, Component.translatable("gui.accept"), Icons.ACCEPT);
			setHeight(12);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			closeGui();
			CompoundTag nbt = new CompoundTag();
			rewardTable.writeData(nbt);
			originalTable.readData(nbt);
			callback.run();
		}
	}

	private class AddWeightedRewardButton extends SimpleTextButton {
		private AddWeightedRewardButton(Panel panel) {
			super(panel, Component.translatable("gui.add"), Icons.ADD);
			setHeight(12);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();

			for (RewardType type : RewardTypes.TYPES.values()) {
				if (!type.getExcludeFromListRewards()) {
					contextMenu.add(new ContextMenuItem(type.getDisplayName(), type.getIconSupplier(), () -> {
						playClickSound();
						type.getGuiProvider().openCreationGui(this, rewardTable.getFakeQuest(), reward -> {
							rewardTable.addReward(new WeightedReward(reward, 1f));
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
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			super.addMouseOverText(list);
			wr.getReward().addMouseOverText(list);
			String w = String.format("%.2f", wr.getWeight());
			String str = WeightedReward.chanceString(wr.getWeight(), rewardTable.getTotalWeight(true));
			list.add(Component.translatable("ftbquests.reward_table.weight").append(": " + w)
					.append(Component.literal(" [" + str + "]").withStyle(ChatFormatting.DARK_GRAY)));
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.edit"), Icons.SETTINGS, () -> {
				ConfigGroup group = new ConfigGroup(FTBQuestsAPI.MOD_ID, accepted -> run());
				wr.getReward().fillConfigGroup(wr.getReward().createSubGroup(group));
				new EditConfigScreen(group).openGui();
			}));

			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.reward_table.set_weight"), Icons.SETTINGS, () -> {
				DoubleConfig c = new DoubleConfig(0D, Double.POSITIVE_INFINITY);
				EditConfigFromStringScreen.open(c, (double) wr.getWeight(), 1D, accepted -> {
					if (accepted) {
						wr.setWeight(c.getValue().floatValue());
					}
					run();
				});
			}));

			contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.delete"), Icons.REMOVE, () -> {
				rewardTable.removeReward(wr);
				EditRewardTableScreen.this.refreshWidgets();
			}).setYesNoText(Component.translatable("delete_item", wr.getReward().getTitle())));
			EditRewardTableScreen.this.openContextMenu(contextMenu);
		}

		@Override
		public Optional<PositionedIngredient> getIngredientUnderMouse() {
			return PositionedIngredient.of(wr.getReward().getIngredient(this), this);
		}
	}

}
