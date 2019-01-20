package com.feed_the_beast.ftbquests.quest.widget;

import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.TextField;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.gui.tree.IQuestWidget;

/**
 * @author LatvianModder
 */
public class TextFieldWidget extends TextField implements IQuestWidget
{
	private final QuestWidgetTextField widget;

	public TextFieldWidget(Panel panel, QuestWidgetTextField w)
	{
		super(panel);
		widget = w;
	}

	@Override
	public QuestWidget getQuestWidget()
	{
		return widget;
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		theme.drawPanelBackground(x, y, w, h);
	}

	@Override
	public void updateScale(GuiQuestTree tree)
	{
		maxWidth = width;
		setText(String.join("\n", widget.text));
	}
}