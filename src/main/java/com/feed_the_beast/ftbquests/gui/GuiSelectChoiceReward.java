package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.net.MessageClaimChoiceReward;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import com.feed_the_beast.ftbquests.quest.reward.ChoiceReward;
import com.feed_the_beast.mods.ftbguilibrary.misc.GuiButtonListBase;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class GuiSelectChoiceReward extends GuiButtonListBase
{
	private class ButtonChoiceReward extends SimpleTextButton
	{
		private final WeightedReward weightedReward;

		private ButtonChoiceReward(Panel panel, WeightedReward r)
		{
			super(panel, r.reward.getTitle(), r.reward.getIcon());
			weightedReward = r;
		}

		@Override
		public void addMouseOverText(TooltipList list)
		{
			super.addMouseOverText(list);
			weightedReward.reward.addMouseOverText(list);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			playClickSound();
			closeGui();
			new MessageClaimChoiceReward(choiceReward.id, choiceReward.getTable().rewards.indexOf(weightedReward)).sendToServer();
		}

		@Override
		@Nullable
		public Object getIngredientUnderMouse()
		{
			return weightedReward.reward.getIngredient();
		}
	}

	private final ChoiceReward choiceReward;

	public GuiSelectChoiceReward(ChoiceReward r)
	{
		choiceReward = r;
		setTitle(new TranslationTextComponent("ftbquests.reward.ftbquests.choice"));
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel)
	{
		for (WeightedReward r : choiceReward.getTable().rewards)
		{
			panel.add(new ButtonChoiceReward(panel, r));
		}
	}

	@Override
	public Theme getTheme()
	{
		return FTBQuestsTheme.INSTANCE;
	}
}