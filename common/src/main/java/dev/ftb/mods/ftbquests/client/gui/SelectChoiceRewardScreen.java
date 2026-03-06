package dev.ftb.mods.ftbquests.client.gui;

import net.minecraft.network.chat.Component;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.screens.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleTextButton;
import dev.ftb.mods.ftblibrary.client.util.PositionedIngredient;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.net.ClaimChoiceRewardMessage;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.ChoiceReward;

import java.util.Optional;
import org.jspecify.annotations.Nullable;

public class SelectChoiceRewardScreen extends AbstractButtonListScreen {
	private final ChoiceReward choiceReward;
	@Nullable
	private WeightedReward acceptedReward;

	public SelectChoiceRewardScreen(ChoiceReward choiceReward) {
		this.choiceReward = choiceReward;

		setTitle(Component.translatable("ftbquests.reward.ftbquests.choice"));
		setBorder(1, 1, 1);
		showBottomPanel(false);
		showCloseButton(true);
	}

	@Override
	public void addButtons(Panel panel) {
		if (choiceReward.getTable() != null) {
			choiceReward.getTable().getWeightedRewards().forEach(wr -> panel.add(new ChoiceRewardButton(panel, wr)));
		}
	}

	@Override
	public Theme getTheme() {
		// use the quests theme rather than ftblib default, since this is a player-facing screen
		return FTBQuestsTheme.INSTANCE;
	}

	@Override
	protected void doCancel() {
		closeGui();
	}

	@Override
	protected void doAccept() {
		closeGui();
		if (choiceReward.getTable() != null && acceptedReward != null) {
			int idx = choiceReward.getTable().getWeightedRewards().indexOf(acceptedReward);
			NetworkManager.sendToServer(new ClaimChoiceRewardMessage(choiceReward.id, idx));
		}
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
			acceptedReward = weightedReward;
			doAccept();
		}

		@Override
		public Optional<PositionedIngredient> getIngredientUnderMouse() {
			return PositionedIngredient.of(weightedReward.getReward().getIngredient(this), this);
		}
	}
}
