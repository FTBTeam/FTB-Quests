package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.GuiRewardNotifications;
import com.feed_the_beast.ftbquests.net.MessageClaimAllRewards;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public class ButtonCollectRewards extends ButtonTab
{
	private static String createTitle(GuiQuestTree treeGui)
	{
		int r = 0;

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
							r++;
						}
					}
				}
			}
		}

		return I18n.format("ftbquests.gui.collect_rewards", TextFormatting.GOLD.toString() + r);
	}

	public ButtonCollectRewards(Panel panel)
	{
		super(panel, createTitle((GuiQuestTree) panel.getGui()), Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/collect_rewards.png"));
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();

		if (ClientQuestFile.existsWithTeam())
		{
			new GuiRewardNotifications().openGui();
			new MessageClaimAllRewards().sendToServer();
		}
	}
}