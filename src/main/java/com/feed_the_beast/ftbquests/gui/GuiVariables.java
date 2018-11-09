package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiButtonListBase;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfigValue;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.QuestVariable;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class GuiVariables extends GuiButtonListBase
{
	public GuiVariables()
	{
		setTitle(I18n.format("ftbquests.variables"));
		setHasSearchBox(true);
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

				new GuiEditConfigValue("id", new ConfigString("", Pattern.compile("^[a-z0-9_]{1,32}$")), (value, set) -> {
					if (set && ClientQuestFile.INSTANCE.get(ClientQuestFile.INSTANCE.getID('#' + value.getString())) == null)
					{
						NBTTagCompound nbt = new NBTTagCompound();
						nbt.setString("id", value.getString());
						new MessageCreateObject(QuestObjectType.VARIABLE, 0, nbt).sendToServer();
					}

					GuiVariables.this.openGui();
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
					ClientQuestFile.INSTANCE.questTreeGui.addObjectMenuItems(contextMenu, getGui(), variable);
					getGui().openContextMenu(contextMenu);
				}
			};

			button.setHeight(14);
			panel.add(button);
		}
	}
}