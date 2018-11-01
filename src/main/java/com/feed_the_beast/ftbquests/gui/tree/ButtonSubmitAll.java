package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.QuestsTheme;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import net.minecraft.client.resources.I18n;

/**
 * @author LatvianModder
 */
public class ButtonSubmitAll extends Button
{
	public ButtonSubmitAll(Panel panel)
	{
		super(panel, I18n.format("ftbquests.gui.submit_all"), QuestsTheme.COMPLETED);
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
	}

	@Override
	public void onClicked(MouseButton button)
	{
		if (!ClientQuestFile.existsWithTeam() || ClientQuestFile.INSTANCE.questTreeGui.selectedQuest == null)
		{
			return;
		}

		GuiHelper.playClickSound();

		for (QuestTask task : ClientQuestFile.INSTANCE.questTreeGui.selectedQuest.tasks)
		{
			task.onButtonClicked();
		}

		for (QuestReward reward : ClientQuestFile.INSTANCE.questTreeGui.selectedQuest.rewards)
		{
			reward.onButtonClicked();
		}
	}
}