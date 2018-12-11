package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfigValue;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.gui.FTBQuestsTheme;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObject;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import net.minecraft.client.resources.I18n;

import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class ButtonAddChapter extends ButtonTab
{
	public ButtonAddChapter(Panel panel)
	{
		super(panel, I18n.format("gui.add"), FTBQuestsTheme.ADD);
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();

		new GuiEditConfigValue("title", new ConfigString("", Pattern.compile("^.+$")), (value, set) ->
		{
			treeGui.openGui();

			if (set)
			{
				QuestChapter chapter = new QuestChapter(treeGui.file);
				chapter.title = value.getString();
				new MessageCreateObject(chapter, null).sendToServer();
			}
		}).openGui();
	}
}