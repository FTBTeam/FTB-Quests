package com.feed_the_beast.ftbquests.gui.chest;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.MessageClaimReward;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestReward;
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
								new MessageClaimReward(reward.uid).sendToServer();
							}
						}
					}
				}
			}
		}
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
								list.add(TextFormatting.GRAY + "- " + reward.stack.getCount() + "x " + StringUtils.unformatted(reward.stack.getDisplayName()) + (reward.team ? (TextFormatting.BLUE + " [" + I18n.format("ftbquests.reward.team_reward") + "]") : ""));
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
