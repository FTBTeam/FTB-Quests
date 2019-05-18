package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.FTBQuestsTheme;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonQuickComplete extends SimpleTextButton
{
	public ButtonQuickComplete(Panel panel)
	{
		super(panel, I18n.format("ftbquests.gui.quick_complete"), FTBQuestsTheme.COMPLETED);
		setHeight(14);
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		super.addMouseOverText(list);
		list.add(TextFormatting.GRAY + I18n.format("ftbquests.gui.quick_complete.info_1"));
		list.add(TextFormatting.GRAY + I18n.format("ftbquests.gui.quick_complete.info_2"));
	}

	@Override
	public void onClicked(MouseButton button)
	{
		if (!ClientQuestFile.existsWithTeam())
		{
			return;
		}

		Quest selectedQuest = ClientQuestFile.INSTANCE.questTreeGui.getViewedQuest();

		if (selectedQuest == null)
		{
			return;
		}

		GuiHelper.playClickSound();

		for (QuestTask task : selectedQuest.tasks)
		{
			task.onButtonClicked();
		}

		for (QuestReward reward : selectedQuest.rewards)
		{
			reward.onButtonClicked();
		}
	}
}