package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftbquests.quest.QuestObjectBase;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

/**
 * @author LatvianModder
 */
public class TabObjectList<T extends QuestObjectBase> extends TabBase implements TreeSelectionListener
{
	public final Class<T> objectClass;
	public final JTree tree;
	public final JPanel panel;
	public T selected = null;

	public TabObjectList(EditorFrame e, Class<T> o, String title, Icon icon)
	{
		super(e);
		objectClass = o;
		setLayout(new GridLayout(0, 1));
		DefaultTreeCellRenderer rootInfo = new DefaultTreeCellRenderer();
		rootInfo.setText(title);
		rootInfo.setIcon(icon);

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootInfo);
		addElements(root);
		tree = new JTree(root);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(this);
		tree.setCellRenderer(IconTreeCellRenderer.INSTANCE);
		ToolTipManager.sharedInstance().registerComponent(tree);
		panel = new JPanel();
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(new JScrollPane(tree));
		splitPane.setRightComponent(new JScrollPane(panel));
		add(splitPane);
	}

	@Override
	public boolean scrollPage()
	{
		return false;
	}

	public void addElements(DefaultMutableTreeNode root)
	{
	}

	public void onSelected()
	{
	}

	@Override
	public void valueChanged(TreeSelectionEvent e)
	{
		Object o = ((DefaultMutableTreeNode) tree.getLastSelectedPathComponent()).getUserObject();
		select(o != null && objectClass.isAssignableFrom(o.getClass()) ? (T) o : null);
	}

	public void select(@Nullable T c)
	{
		if (selected != c)
		{
			selected = c;
			panel.removeAll();
			onSelected();
		}
	}
}