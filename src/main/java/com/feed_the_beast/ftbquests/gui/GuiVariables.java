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
import com.feed_the_beast.ftbquests.quest.QuestVariable;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiVariables extends GuiButtonListBase
{
	public GuiVariables()
	{
		setTitle(I18n.format("ftbquests.variables"));
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
					GuiVariables.this.openGui();

					if (set)
					{
						QuestVariable variable = new QuestVariable(ClientQuestFile.INSTANCE);
						variable.title = value.getString();
						new MessageCreateObject(variable, null).sendToServer();
					}
				}).openGui();
			}
		};

		button.setHeight(14);
		panel.add(button);

		for (QuestVariable variable : ClientQuestFile.INSTANCE.variables)
		{
			button = new SimpleTextButton(panel, variable.getDisplayName().getFormattedText(), variable.getIcon())
			{
				@Override
				public void onClicked(MouseButton button)
				{
					GuiHelper.playClickSound();
					List<ContextMenuItem> contextMenu = new ArrayList<>();
					GuiQuestTree.addObjectMenuItems(contextMenu, getGui(), variable);
					getGui().openContextMenu(contextMenu);
				}
			};

			button.setHeight(14);
			panel.add(button);
		}
	}

	@Override
	public Theme getTheme()
	{
		return QuestsTheme.INSTANCE;
	}
}