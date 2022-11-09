package dev.ftb.mods.ftbquests.gui;

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
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class EditRewardTableScreen extends ButtonListBaseScreen {
	private class RewardTableSettingsButton extends SimpleTextButton {
		private RewardTableSettingsButton(Panel panel) {
			super(panel, Component.translatable("gui.settings"), Icons.SETTINGS);
			setHeight(12);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
			rewardTable.getConfig(rewardTable.createSubGroup(group));
			group.savedCallback = accepted -> run();
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
					contextMenu.add(new ContextMenuItem(type.getDisplayName(), type.getIcon(), () -> {
						playClickSound();
						type.getGuiProvider().openCreationGui(this, rewardTable.fakeQuest, reward -> {
							rewardTable.rewards.add(new WeightedReward(reward, 1));
							openGui();
						});
					}));
				}
			}

			getGui().openContextMenu(contextMenu);
		}
	}

	private class WeightedRewardButton extends SimpleTextButton {
		private final WeightedReward reward;

		private WeightedRewardButton(Panel panel, WeightedReward r) {
			super(panel, r.reward.getTitle(), r.reward.getIcon());
			reward = r;
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			super.addMouseOverText(list);
			reward.reward.addMouseOverText(list);
			list.add(Component.translatable("ftbquests.reward_table.weight").append(": " + reward.weight).append(Component.literal(" [" + WeightedReward.chanceString(reward.weight, rewardTable.getTotalWeight(true)) + "]").withStyle(ChatFormatting.DARK_GRAY)));
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.edit"), Icons.SETTINGS, () -> {
				ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
				reward.reward.getConfig(reward.reward.createSubGroup(group));
				group.savedCallback = accepted -> run();
				new EditConfigScreen(group).openGui();
			}));

			contextMenu.add(new ContextMenuItem(Component.translatable("ftbquests.reward_table.set_weight"), Icons.SETTINGS, () -> {
				DoubleConfig c = new DoubleConfig(0D, Double.POSITIVE_INFINITY);
				EditConfigFromStringScreen.open(c, (double) reward.weight, 1D, accepted -> {
					if (accepted) {
						reward.weight = c.value.intValue();

						if (c.value < 1D) {
							for (WeightedReward reward : rewardTable.rewards) {
								reward.weight = (int) (reward.weight / c.value);
							}

							reward.weight = 1;
						}
					}

					run();
				});
			}));

			contextMenu.add(new ContextMenuItem(Component.translatable("selectServer.delete"), Icons.REMOVE, () -> {
				rewardTable.rewards.remove(reward);
				EditRewardTableScreen.this.refreshWidgets();
			}).setYesNo(Component.translatable("delete_item", reward.reward.getTitle())));
			EditRewardTableScreen.this.openContextMenu(contextMenu);
		}

		@Override
		@Nullable
		public Object getIngredientUnderMouse() {
			return reward.reward.getIngredient();
		}
	}

	private final RewardTable originalTable;
	private final RewardTable rewardTable;
	private final Runnable callback;

	public EditRewardTableScreen(RewardTable r, Runnable c) {
		originalTable = r;
		rewardTable = new RewardTable(originalTable.file);
		CompoundTag nbt = new CompoundTag();
		originalTable.writeData(nbt);
		rewardTable.readData(nbt);
		callback = c;
		setTitle(Component.translatable("ftbquests.reward_table").append(": " + rewardTable.title));
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel) {
		panel.add(new RewardTableSettingsButton(panel));
		panel.add(new SaveRewardTableButton(panel));
		panel.add(new AddWeightedRewardButton(panel));
		panel.add(new VerticalSpaceWidget(panel, 1));

		for (WeightedReward r : rewardTable.rewards) {
			panel.add(new WeightedRewardButton(panel, r));
		}
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
}
