package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;

/**
 * @author LatvianModder
 */
public class PanelOtherButtons extends Panel
{
	public final GuiQuestTree treeGui;

	public PanelOtherButtons(Panel panel)
	{
		super(panel);
		treeGui = (GuiQuestTree) panel.getGui();
	}

	private boolean hasRewards()
	{
		if (treeGui.questFile.self == null)
		{
			return false;
		}

		for (QuestChapter chapter : treeGui.questFile.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				if (quest.isComplete(treeGui.questFile.self))
				{
					for (QuestReward reward : quest.rewards)
					{
						if (!treeGui.questFile.isRewardClaimed(reward))
						{
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public void addWidgets()
	{
		if (hasRewards())
		{
			add(new ButtonClaimAllRewards(this));
		}

		if (!treeGui.questFile.emergencyItems.isEmpty() && (treeGui.questFile.self != null || treeGui.questFile.canEdit()))
		{
			add(new ButtonEmergencyItems(this));
		}

		add(new ButtonWiki(this));

		if (treeGui.questFile.canEdit())
		{
			add(new ButtonEditSettings(this));
		}
	}

	@Override
	public void alignWidgets()
	{
		setSize(align(new WidgetLayout.Horizontal(1, 1, 0)), treeGui.chapterPanel.height);
		setX(getGui().width - width - 1);
	}
}