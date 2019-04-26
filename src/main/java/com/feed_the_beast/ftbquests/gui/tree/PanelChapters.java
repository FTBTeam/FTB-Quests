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
		boolean canEdit = treeGui.file.canEdit();

		for (int i = 0; i < treeGui.file.chapters.size(); i++)
		{
			QuestChapter chapter = treeGui.file.chapters.get(i);

			if (canEdit || chapter.isVisible(treeGui.file.self))
			{
				add(new ButtonChapter(this, chapter));
			}
		}
	}

	@Override
	public void alignWidgets()
	{
		setX(1);
		setWidth(treeGui.width - treeGui.otherButtons.width - 2);

		if (!widgets.isEmpty())
		{
			/*int w = width / widgets.size();

			for (Widget widget : widgets)
			{
				widget.setWidth(w);
			}*/

			align(new WidgetLayout.Horizontal(0, 1, 0));
		}
	}
}