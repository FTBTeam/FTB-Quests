package dev.ftb.mods.ftbquests.client.gui;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.ButtonListBaseScreen;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.net.ClaimChoiceRewardMessage;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.ChoiceReward;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class SelectChoiceRewardScreen extends ButtonListBaseScreen {
	private final ChoiceReward choiceReward;

	public SelectChoiceRewardScreen(ChoiceReward choiceReward) {
		this.choiceReward = choiceReward;

		setTitle(Component.translatable("ftbquests.reward.ftbquests.choice"));
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel) {
		if (choiceReward.getTable() != null) {
			choiceReward.getTable().getWeightedRewards().forEach(wr -> panel.add(new ChoiceRewardButton(panel, wr)));
		}
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}

	private class ChoiceRewardButton extends SimpleTextButton {
		private final WeightedReward weightedReward;

		private ChoiceRewardButton(Panel panel, WeightedReward wr) {
			super(panel, wr.getReward().getTitle(), wr.getReward().getIcon());
			weightedReward = wr;
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			super.addMouseOverText(list);
			weightedReward.getReward().addMouseOverText(list);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			closeGui();
			if (choiceReward.getTable() != null) {
				new ClaimChoiceRewardMessage(choiceReward.id, choiceReward.getTable().getWeightedRewards().indexOf(weightedReward)).sendToServer();
			}
		}

		@Override
		public Optional<PositionedIngredient> getIngredientUnderMouse() {
			return PositionedIngredient.of(weightedReward.getReward().getIngredient(this), this);
		}
	}
}
