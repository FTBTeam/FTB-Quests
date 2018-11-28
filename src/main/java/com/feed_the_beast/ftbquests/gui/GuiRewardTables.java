package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiButtonListBase;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfigValue;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObject;
import com.feed_the_beast.ftbquests.quest.reward.RewardTable;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiRewardTables extends GuiButtonListBase
{
	private class ButtonRewardTable extends SimpleTextButton
	{
		private final RewardTable table;

		public ButtonRewardTable(Panel panel, RewardTable t)
		{
			super(panel, t.getDisplayName().getFormattedText(), t.getIcon());
			table = t;
			setHeight(14);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			List<ContextMenuItem> contextMenu = new ArrayList<>();
			GuiQuestTree.addObjectMenuItems(contextMenu, GuiRewardTables.this, table);
			getGui().openContextMenu(contextMenu);
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			super.addMouseOverText(list);
			table.addMouseOverText(list, true, true);
		}
	}

	public GuiRewardTables()
	{
		setTitle(I18n.format("ftbquests.reward_tables"));
		setHasSearchBox(true);
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel)
	{
		SimpleTextButton button = new SimpleTextButton(panel, I18n.format("gui.add"), GuiIcons.ADD)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				GuiHelper.playClickSound();

				new GuiEditConfigValue("id", new ConfigString(""), (value, set) -> {
					GuiRewardTables.this.openGui();

					if (set)
					{
						RewardTable table = new RewardTable(ClientQuestFile.INSTANCE);
						table.title = value.getString();
						new MessageCreateObject(table, null).sendToServer();
					}
				}).openGui();
			}
		};

		button.setHeight(14);
		panel.add(button);

		for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables)
		{
			panel.add(new ButtonRewardTable(panel, table));
		}
	}

	@Override
	public Theme getTheme()
	{
		return FTBQuestsTheme.INSTANCE;
	}
}