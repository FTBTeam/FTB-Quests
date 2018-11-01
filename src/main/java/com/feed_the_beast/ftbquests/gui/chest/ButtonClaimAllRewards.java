package com.feed_the_beast.ftbquests.gui.chest;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.MessageClaimAllRewards;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
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
								ITextComponent component = new TextComponentString("");
								component.getStyle().setColor(TextFormatting.GRAY);
								component.appendText("- ");
								component.appendSibling(reward.getDisplayName());

								if (reward.isTeamReward())
								{
									ITextComponent component1 = new TextComponentString("");
									component1.getStyle().setColor(TextFormatting.BLUE);
									component1.appendText(" [");
									component1.appendSibling(new TextComponentTranslation("ftbquests.reward.team_reward"));
									component1.appendText("]");
									component.appendSibling(component1);
								}

								list.add(component.getFormattedText());
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
