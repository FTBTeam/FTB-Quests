package dev.ftb.mods.ftbquests.gui;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.ButtonListBaseScreen;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.net.ClaimChoiceRewardPacket;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.ChoiceReward;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class SelectChoiceRewardScreen extends ButtonListBaseScreen {
	private class ChoiceRewardButton extends SimpleTextButton {
		private final WeightedReward weightedReward;

		private ChoiceRewardButton(Panel panel, WeightedReward r) {
			super(panel, r.reward.getTitle(), r.reward.getIcon());
			weightedReward = r;
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			super.addMouseOverText(list);
			weightedReward.reward.addMouseOverText(list);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			closeGui();
			new ClaimChoiceRewardPacket(choiceReward.id, choiceReward.getTable().rewards.indexOf(weightedReward)).sendToServer();
		}

		@Override
		@Nullable
		public Object getIngredientUnderMouse() {
			return weightedReward.reward.getIngredient();
		}
	}

	private final ChoiceReward choiceReward;

	public SelectChoiceRewardScreen(ChoiceReward r) {
		choiceReward = r;
		setTitle(new TranslatableComponent("ftbquests.reward.ftbquests.choice"));
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel) {
		for (WeightedReward r : choiceReward.getTable().rewards) {
			panel.add(new ChoiceRewardButton(panel, r));
		}
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}
}