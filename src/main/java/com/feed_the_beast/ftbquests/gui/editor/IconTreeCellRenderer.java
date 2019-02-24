package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftbquests.quest.QuestObjectBase;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * @author LatvianModder
 */
public class IconTreeCellRenderer extends DefaultTreeCellRenderer
{
	public static final IconTreeCellRenderer INSTANCE = new IconTreeCellRenderer();

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (value instanceof DefaultMutableTreeNode)
		{
			Object info = ((DefaultMutableTreeNode) value).getUserObject();

			if (info instanceof DefaultTreeCellRenderer)
			{
				DefaultTreeCellRenderer r = (DefaultTreeCellRenderer) info;
				setText(r.getText());
				setToolTipText(r.getToolTipText());
				setIcon(r.getIcon());
			}
			else if (info instanceof QuestObjectBase)
			{
				QuestObjectBase o = (QuestObjectBase) info;

				setText(EditorFrame.toString(o.getDisplayName()));
				setToolTipText(o.getCodeString());
				setIcon(o.getIcon().getWrappedIcon());
			}
		}

		return this;
	}
}