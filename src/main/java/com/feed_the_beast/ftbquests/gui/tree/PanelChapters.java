package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
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
		setPosAndSize(0, 1, 18, treeGui.height - 2);
		align(WidgetLayout.VERTICAL);
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		treeGui.borderColor.draw(x + w - 1, y, 1, h);
		treeGui.backgroundColor.draw(x + 1, y, w - 2, h);
	}
}