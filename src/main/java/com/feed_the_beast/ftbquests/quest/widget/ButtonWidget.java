package com.feed_the_beast.ftbquests.quest.widget;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.gui.tree.IQuestWidget;

/**
 * @author LatvianModder
 */
public class ButtonWidget extends SimpleTextButton implements IQuestWidget
{
	private final QuestWidgetButton widget;

	public ButtonWidget(Panel p, QuestWidgetButton w)
	{
		super(p, w.title, Icon.EMPTY);
		widget = w;
	}

	@Override
	public QuestWidget getQuestWidget()
	{
		return widget;
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		handleClick(widget.click);
	}
}