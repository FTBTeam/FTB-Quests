package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftbquests.quest.QuestChapter;

/**
 * @author LatvianModder
 */
public class PanelChapters extends Panel
{
	public final GuiQuestTree treeGui;

	public PanelChapters(Panel panel)
	{
		super(panel);
		treeGui = (GuiQuestTree) panel.getGui();
	}

	@Override
	public void addWidgets()
	{
		boolean canEdit = treeGui.questFile.canEdit();

		for (int i = 0; i < treeGui.questFile.chapters.size(); i++)
		{
			QuestChapter chapter = treeGui.questFile.chapters.get(i);

			if (canEdit || !chapter.quests.isEmpty())
			{
				add(new ButtonChapter(this, i, chapter));
			}
		}

		if (canEdit)
		{
			add(new ButtonAddChapter(this));
		}
	}

	@Override
	public void alignWidgets()
	{
		setX(1);
		setWidth(treeGui.width - treeGui.otherButtons.width - 2);
		align(new WidgetLayout.Horizontal(0, 1, 0));
	}
}