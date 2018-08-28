package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.MessageClaimReward;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestReward;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class GuiRewards extends GuiBase
{
	public class ButtonReward extends Button
	{
		public final QuestReward reward;

		public ButtonReward(Panel panel, QuestReward r)
		{
			super(panel, r.stack.getDisplayName() + (r.team ? TextFormatting.BLUE + " [" + I18n.format("ftbquests.reward.team_reward") + "]" : ""), ItemIcon.getItemIcon(r.stack));
			setSize(20, 20);
			reward = r;
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			if (isShiftKeyDown() && isCtrlKeyDown())
			{
				list.add(getTitle() + " " + TextFormatting.DARK_GRAY + reward);
			}
			else
			{
				list.add(getTitle());
			}
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			new MessageClaimReward(reward.uid).sendToServer();
		}

		@Override
		public void drawBackground(Theme theme, int x, int y, int w, int h)
		{
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
			QuestsTheme.BUTTON.draw(x - 3, y - 3, w + 6, h + 6);

			if (isMouseOver())
			{
				QuestsTheme.BUTTON.draw(x - 3, y - 3, w + 6, h + 6);
			}

			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

			if (!ClientQuestFile.existsWithTeam())
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 500);
				GuiIcons.CLOSE.draw(x + w - 9, y + 1, 8, 8);
				GlStateManager.popMatrix();
			}
			else if (ClientQuestFile.INSTANCE.self.isRewardClaimed(ClientUtils.MC.player.getUniqueID(), reward))
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 500);
				QuestsTheme.COMPLETED.draw(x + w - 9, y + 1, 8, 8);
				GlStateManager.popMatrix();
			}
		}
	}

	public final Button claimAll;
	public final Panel rewards;

	public GuiRewards()
	{
		claimAll = new SimpleTextButton(this, I18n.format("ftbquests.reward.claim_all"), Icon.EMPTY)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				GuiHelper.playClickSound();

				for (Widget widget : rewards.widgets)
				{
					new MessageClaimReward(((ButtonReward) widget).reward.uid).sendToServer();
				}

				rewards.widgets.clear();
				refreshWidgets();
			}

			@Override
			public WidgetType getWidgetType()
			{
				return rewards.widgets.isEmpty() ? WidgetType.DISABLED : super.getWidgetType();
			}
		};

		rewards = new Panel(this)
		{
			@Override
			public void addWidgets()
			{
				if (ClientQuestFile.existsWithTeam())
				{
					UUID id = ClientUtils.MC.player.getUniqueID();

					for (QuestChapter chapter : ClientQuestFile.INSTANCE.chapters)
					{
						for (Quest quest : chapter.quests)
						{
							if (quest.isComplete(ClientQuestFile.INSTANCE.self))
							{
								for (QuestReward reward : quest.rewards)
								{
									if (!ClientQuestFile.INSTANCE.self.isRewardClaimed(id, reward))
									{
										add(new ButtonReward(this, reward));
									}
								}
							}
						}
					}
				}
			}

			@Override
			public void alignWidgets()
			{
				for (int i = 0; i < widgets.size(); i++)
				{
					widgets.get(i).setPos((i % 9) * 25 + 5, (i / 9) * 25 + 5);
				}

				setSize(Math.min(widgets.size(), 9) * 25 + 5, (1 + (widgets.size() / 9)) * 25 + 5);
				setPos((getGui().width - width) / 2, 40);
			}
		};
	}

	@Override
	public void addWidgets()
	{
		add(claimAll);
		add(rewards);

		claimAll.setPos((width - claimAll.width) / 2, height - 61);
	}

	@Override
	public boolean onInit()
	{
		return setFullscreen();
	}

	@Override
	public Theme getTheme()
	{
		return QuestsTheme.INSTANCE;
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		if (rewards.widgets.isEmpty())
		{
			String text = "All rewards have been claimed";
			theme.drawString(text, x + (w - theme.getStringWidth(text)) / 2, y + h / 2 - 30);
		}
	}
}