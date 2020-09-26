package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.net.MessageCreateObject;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigString;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfigFromString;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import net.minecraft.client.resources.I18n;

import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class ButtonAddChapter extends ButtonTab
{
	public ButtonAddChapter(Panel panel)
	{
		super(panel, I18n.format("gui.add"), ThemeProperties.ADD_ICON.get());
	}

	@Override
	public void onClicked(MouseButton button)
	{
		playClickSound();

		ConfigString c = new ConfigString(Pattern.compile("^.+$"));
		GuiEditConfigFromString.open(c, "", "", accepted -> {
			treeGui.openGui();

			if (accepted && !c.value.trim().isEmpty())
			{
				Chapter chapter = new Chapter(treeGui.file);
				chapter.title = c.value.trim();
				new MessageCreateObject(chapter, null).sendToServer();
			}

			run();
		});
	}
}