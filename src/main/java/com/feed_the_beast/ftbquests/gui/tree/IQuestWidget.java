package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftbquests.quest.widget.QuestWidget;

/**
 * @author LatvianModder
 */
public interface IQuestWidget
{
	QuestWidget getQuestWidget();

	default void updateScale(GuiQuestTree tree)
	{
	}
}