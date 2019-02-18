package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import net.minecraft.client.resources.I18n;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author LatvianModder
 */
public class TabChapters extends TabObjectList<QuestChapter>
{
	public TabChapters(EditorFrame e)
	{
		super(e, QuestChapter.class, I18n.format("ftbquests.chapters"), IconWrapper.from(GuiIcons.COLOR_RGB));
	}

	@Override
	public void addElements(DefaultMutableTreeNode root)
	{
		for (QuestChapter chapter : editor.file.chapters)
		{
			root.add(new DefaultMutableTreeNode(chapter));
		}
	}

	@Override
	public void onSelected()
	{
		if (selected != null)
		{
			panel.add(new JTextArea(selected.getDisplayName().getUnformattedText()));
		}
	}
}