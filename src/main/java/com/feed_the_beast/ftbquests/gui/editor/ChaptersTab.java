package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import net.minecraft.client.resources.I18n;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author LatvianModder
 */
public class ChaptersTab extends ObjectListTab<QuestChapter>
{
	public ChaptersTab(EditorFrame e)
	{
		super(e, QuestChapter.class, I18n.format("ftbquests.chapters"), GuiIcons.COLOR_RGB.getWrappedIcon());
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