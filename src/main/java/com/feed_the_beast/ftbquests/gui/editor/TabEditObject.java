package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;

/**
 * @author LatvianModder
 */
public class TabEditObject extends Tab
{
	public final QuestObjectBase object;

	public TabEditObject(QuestObjectBase o)
	{
		object = o;
		setText(object.getUnformattedTitle());
		setTooltip(new Tooltip(object.toString()));
		setContent(new ScrollPane(object.createTabContent().get()));
		Editor.loadIcon(this, object.getIcon());
	}
}