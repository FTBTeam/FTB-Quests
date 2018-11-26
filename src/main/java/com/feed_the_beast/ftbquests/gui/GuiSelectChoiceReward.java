package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiButtonListBase;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.net.MessageClaimChoiceReward;
import com.feed_the_beast.ftbquests.quest.reward.ChoiceReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import net.minecraft.client.resources.I18n;

import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiSelectChoiceReward extends GuiButtonListBase
{
	private class ButtonChoiceReward extends SimpleTextButton
	{
		private final QuestReward reward;

		private ButtonChoiceReward(Panel panel, QuestReward r)
		{
			super(panel, r.getDisplayName().getFormattedText(), r.getIcon());
			reward = r;
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			super.addMouseOverText(list);
			reward.addMouseOverText(list);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			closeGui();
			new MessageClaimChoiceReward(choiceReward.uid, choiceReward.rewards.indexOf(reward)).sendToServer();
		}
	}

	private final ChoiceReward choiceReward;

	public GuiSelectChoiceReward(ChoiceReward r)
	{
		choiceReward = r;
		setTitle(I18n.format("ftbquests.reward.ftbquests.choice"));
	}

	@Override
	public void addButtons(Panel panel)
	{
		for (QuestReward r : choiceReward.rewards)
		{
			panel.add(new ButtonChoiceReward(panel, r));
		}
	}
}