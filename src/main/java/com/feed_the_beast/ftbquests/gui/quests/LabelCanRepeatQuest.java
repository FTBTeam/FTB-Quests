package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import net.minecraft.client.resources.I18n;

import java.util.List;

/**
 * @author LatvianModder
 */
public class LabelCanRepeatQuest extends Widget
{
	public LabelCanRepeatQuest(Panel panel)
	{
		super(panel);
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		list.add(I18n.format("ftbquests.quest.can_repeat"));
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		GuiIcons.REFRESH.draw(x, y, w, h);
	}
}