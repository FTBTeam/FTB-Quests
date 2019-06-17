package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import javafx.scene.control.TreeItem;

/**
 * @author LatvianModder
 */
public class QuestObjectBaseTreeItem extends TreeItem<QuestObjectBase> implements Comparable<QuestObjectBaseTreeItem>
{
	public QuestObjectBaseTreeItem(QuestObjectBase object)
	{
		super(object);
	}

	@Override
	public int compareTo(QuestObjectBaseTreeItem o)
	{
		int i = getValue().getObjectType().compareTo(o.getValue().getObjectType());
		return i == 0 ? getValue().getUnformattedTitle().compareToIgnoreCase(o.getValue().getUnformattedTitle()) : i;
	}
}