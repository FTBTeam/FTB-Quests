package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.TextField;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.gui.WidgetVerticalSpace;
import com.feed_the_beast.ftblib.lib.util.StringJoiner;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class PanelQuestRight extends Panel
{
	public final GuiQuestTree treeGui;

	public PanelQuestRight(Panel panel)
	{
		super(panel);
		treeGui = (GuiQuestTree) panel.getGui();
	}

	@Override
	public void addWidgets()
	{
		if (treeGui.selectedQuest != null && !treeGui.movingQuest)
		{
			setY(treeGui.chapterPanel.height + 1);

			List<Widget> afterText = new ArrayList<>();

			afterText.add(new TextField(this).setText(TextFormatting.GOLD + I18n.format("ftbquests.rewards") + ":"));

			if (treeGui.selectedQuest.rewards.isEmpty())
			{
				afterText.add(new TextField(this).setText(TextFormatting.GRAY + I18n.format("ftbquests.gui.no_rewards")));
			}
			else
			{
				for (QuestReward reward : treeGui.selectedQuest.rewards)
				{
					afterText.add(new ButtonReward(this, reward));
				}
			}

			if (treeGui.questFile.canEdit())
			{
				afterText.add(new ButtonAddReward(this, treeGui.selectedQuest));
			}

			setWidth(80);

			if (!treeGui.selectedQuest.description.isEmpty())
			{
				setWidth(Math.max(width, Math.min(180, treeGui.getTheme().getStringWidth(treeGui.selectedQuest.description) + 6)));
			}

			if (!treeGui.selectedQuest.text.isEmpty())
			{
				setWidth(Math.max(width, 180));
			}

			for (Widget widget : afterText)
			{
				setWidth(Math.max(width, widget.width + 6));
			}

			if (width > 150)
			{
				setWidth(150);
			}

			if (!treeGui.selectedQuest.description.isEmpty())
			{
				add(new TextField(this).setMaxWidth(width - 3).setSpacing(9).setText(TextFormatting.GRAY.toString() + TextFormatting.ITALIC + StringUtils.unformatted(treeGui.selectedQuest.description).replace("&(\\S)", StringUtils.FORMATTING_CHAR + "$1")));
				add(new WidgetVerticalSpace(this, 5));
			}

			if (!treeGui.selectedQuest.text.isEmpty())
			{
				add(new TextField(this).setMaxWidth(width - 3).setSpacing(9).setText(StringJoiner.with('\n').join(treeGui.selectedQuest.text).replace("&(\\S)", StringUtils.FORMATTING_CHAR + "$1")));
				add(new WidgetVerticalSpace(this, 10));
			}

			addAll(afterText);

			setHeight(Math.min(treeGui.height - treeGui.chapterPanel.height - 3, align(new WidgetLayout.Vertical(3, 3, 3))));

			for (Widget widget : widgets)
			{
				widget.setWidth(width - 6);
				widget.setX(3);
			}

			setX(treeGui.width - width - 2);
		}

		if (widgets.isEmpty())
		{
			setPosAndSize(-100, 0, 0, 0);
		}
	}

	@Override
	public void alignWidgets()
	{
	}

	@Override
	public boolean mousePressed(MouseButton button)
	{
		return super.mousePressed(button) || isMouseOver();
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate(0F, 0F, 500F);
		super.draw(theme, x, y, w, h);
		GlStateManager.popMatrix();
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		if (w > 0)
		{
			GuiHelper.drawHollowRect(x, y, w, h, treeGui.borderColor, false);
			theme.drawGui(x + 1, y + 1, w - 2, h - 2, WidgetType.DISABLED);
		}
	}
}