package dev.ftb.mods.ftbquests.gui;

import com.feed_the_beast.mods.ftbguilibrary.misc.GuiButtonListBase;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import dev.ftb.mods.ftbquests.net.MessageClaimChoiceReward;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.ChoiceReward;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class SelectChoiceRewardScreen extends GuiButtonListBase {
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
			new MessageClaimChoiceReward(choiceReward.id, choiceReward.getTable().rewards.indexOf(weightedReward)).sendToServer();
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