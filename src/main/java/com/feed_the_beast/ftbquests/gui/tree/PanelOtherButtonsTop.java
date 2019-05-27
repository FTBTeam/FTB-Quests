package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;

/**
 * @author LatvianModder
 */
public class PanelOtherButtonsTop extends PanelOtherButtons
{
	public PanelOtherButtonsTop(Panel panel)
	{
		super(panel);
	}

	private boolean hasRewards()
	{
		if (treeGui.file.self == null)
		{
			return false;
		}

		for (QuestChapter chapter : treeGui.file.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				if (quest.isComplete(treeGui.file.self))
				{
					for (QuestReward reward : quest.rewards)
					{
						if (!treeGui.file.isRewardClaimed(reward))
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
			add(new ButtonCollectRewards(this));
		}

		add(new ButtonWiki(this));

		if (!treeGui.file.emergencyItems.isEmpty() && (treeGui.file.self != null || treeGui.file.canEdit()))
		{
			add(new ButtonEmergencyItems(this));
		}
	}

	@Override
	public void alignWidgets()
	{
		setPosAndSize(treeGui.width - width, 1, width, align(WidgetLayout.VERTICAL));
	}
}