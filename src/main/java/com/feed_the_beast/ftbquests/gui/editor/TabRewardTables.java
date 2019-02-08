package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import net.minecraft.client.resources.I18n;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author LatvianModder
 */
public class TabRewardTables extends TabObjectList<RewardTable>
{
	public TabRewardTables(FrameEditor e)
	{
		super(e, RewardTable.class, I18n.format("ftbquests.reward_tables"), IconWrapper.from(GuiIcons.MONEY_BAG));
	}

	@Override
	public void addElements(DefaultMutableTreeNode root)
	{
		for (RewardTable table : editor.file.rewardTables)
		{
			root.add(new DefaultMutableTreeNode(table));
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