package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftbquests.quest.QuestVariable;
import net.minecraft.client.resources.I18n;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author LatvianModder
 */
public class TabVariables extends TabObjectList<QuestVariable>
{
	public TabVariables(FrameEditor e)
	{
		super(e, QuestVariable.class, I18n.format("ftbquests.variables"), IconWrapper.from(GuiIcons.CONTROLLER));
	}

	@Override
	public void addElements(DefaultMutableTreeNode root)
	{
		for (QuestVariable variable : editor.file.variables)
		{
			root.add(new DefaultMutableTreeNode(variable));
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