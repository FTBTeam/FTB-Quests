package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetLayout;
import net.minecraftforge.fml.ModList;

/**
 * @author LatvianModder
 */
public class PanelOtherButtonsTop extends PanelOtherButtons
{
	public PanelOtherButtonsTop(Panel panel)
	{
		super(panel);
	}

	@Override
	public void addWidgets()
	{
		add(new ButtonModpack(this));

		if (ModList.get().isLoaded("ftbguides"))
		{
			add(new ButtonOpenGuides(this));
		}

		if (!treeGui.file.emergencyItems.isEmpty() && (treeGui.file.self != null || treeGui.file.canEdit()))
		{
			add(new ButtonEmergencyItems(this));
		}

		if (!ThemeProperties.WIKI_URL.get().equals("-"))
		{
			add(new ButtonWiki(this));
		}
	}

	@Override
	public void alignWidgets()
	{
		setPosAndSize(treeGui.width - width, 1, width, align(WidgetLayout.VERTICAL));
	}
}