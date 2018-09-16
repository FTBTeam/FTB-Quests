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
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class PanelQuestLeft extends Panel
{
	public final GuiQuestTree treeGui;

	public PanelQuestLeft(Panel panel)
	{
		super(panel);
		treeGui = (GuiQuestTree) panel.getGui();
	}

	@Override
	public void addWidgets()
	{
		if (treeGui.selectedQuest != null && !treeGui.movingQuest)
		{
			setPos(2, treeGui.chapterPanel.height + 1);
			add(new TextField(this).setScale(1.5F).addFlags(Theme.CENTERED).setText(treeGui.selectedQuest.getDisplayName().getFormattedText()));
			add(new WidgetVerticalSpace(this, 2));
			add(new TextField(this).setText(TextFormatting.BLUE + I18n.format("ftbquests.tasks") + ":"));

			for (QuestTask task : treeGui.selectedQuest.tasks)
			{
				add(new ButtonTask(this, task));
			}

			if (treeGui.questFile.canEdit())
			{
				add(new ButtonAddTask(this, treeGui.selectedQuest));
			}

			List<String> dependencies = new ArrayList<>();

			for (QuestObject object : treeGui.selectedQuest.getDependencies())
			{
				if (dependencies.isEmpty())
				{
					add(new WidgetVerticalSpace(this, 2));
					add(new TextField(this).setText(TextFormatting.AQUA + I18n.format("ftbquests.gui.requires") + ":"));
				}

				dependencies.add(TextFormatting.GRAY + object.getDisplayName().getUnformattedText());
			}

			if (!dependencies.isEmpty())
			{
				add(new TextField(this).setSpacing(10).setText(StringJoiner.with('\n').join(dependencies)));
			}

			List<String> dependants = new ArrayList<>();

			for (QuestChapter chapter : treeGui.questFile.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					if (quest.hasDependency(treeGui.selectedQuest))
					{
						if (dependants.isEmpty())
						{
							add(new WidgetVerticalSpace(this, 2));
							add(new TextField(this).setText(TextFormatting.YELLOW + I18n.format("ftbquests.gui.required_by") + ":"));
						}

						dependants.add(TextFormatting.GRAY + quest.getDisplayName().getUnformattedText());
					}
				}
			}

			if (!dependants.isEmpty())
			{
				add(new TextField(this).setSpacing(10).setText(StringJoiner.with('\n').join(dependants)));
			}

			setWidth(100);

			for (Widget widget : widgets)
			{
				setWidth(Math.max(width, widget.width + 6));
			}

			setHeight(Math.min(treeGui.height - treeGui.chapterPanel.height - 3, align(new WidgetLayout.Vertical(3, 3, 3))));

			for (Widget widget : widgets)
			{
				widget.setWidth(width - 6);
				widget.setX(3);
			}
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