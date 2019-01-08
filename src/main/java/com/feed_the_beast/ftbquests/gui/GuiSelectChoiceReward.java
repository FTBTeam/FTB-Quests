package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiButtonListBase;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.net.MessageClaimChoiceReward;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import com.feed_the_beast.ftbquests.quest.reward.ChoiceReward;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;
import java.util.List;

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
			super(panel, r.reward.getDisplayName().getFormattedText(), r.reward.getIcon());
			weightedReward = r;
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			super.addMouseOverText(list);
			weightedReward.reward.addMouseOverText(list);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			closeGui();
			new MessageClaimChoiceReward(choiceReward.id, choiceReward.getTable().rewards.indexOf(weightedReward)).sendToServer();
		}

		@Override
		@Nullable
		public Object getJEIFocus()
		{
			return weightedReward.reward.getJEIFocus();
		}
	}

	private final ChoiceReward choiceReward;

	public GuiSelectChoiceReward(ChoiceReward r)
	{
		choiceReward = r;
		setTitle(I18n.format("ftbquests.reward.ftbquests.choice"));
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