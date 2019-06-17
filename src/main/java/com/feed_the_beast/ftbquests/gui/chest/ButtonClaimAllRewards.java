package com.feed_the_beast.ftbquests.gui.chest;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.GuiRewardNotifications;
import com.feed_the_beast.ftbquests.net.MessageClaimAllRewards;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonClaimAllRewards extends Button
{
	public ButtonClaimAllRewards(Panel panel)
	{
		super(panel, I18n.format("ftbquests.reward.claim_all"), Icon.EMPTY);
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		new GuiRewardNotifications().openGui();
		new MessageClaimAllRewards().sendToServer();
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		list.add(getTitle());

		if (ClientQuestFile.existsWithTeam())
		{
			for (QuestChapter chapter : ClientQuestFile.INSTANCE.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					if (quest.isComplete(ClientQuestFile.INSTANCE.self))
					{
						for (QuestReward reward : quest.rewards)
						{
							if (!ClientQuestFile.INSTANCE.isRewardClaimed(reward))
							{
								String s = TextFormatting.GRAY + "- " + reward.getTitle();

								if (reward.isTeamReward())
								{
									s += TextFormatting.BLUE + " [" + I18n.format("ftbquests.reward.team_reward") + "]";
								}

								list.add(s);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		if (isMouseOver())
		{
			Color4I.WHITE.withAlpha(33).draw(x, y, w, h);
		}
	}
}
