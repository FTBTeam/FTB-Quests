package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.ChapterGroup;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.widget.ColorWidget;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetLayout;
import me.shedaniel.architectury.platform.Platform;

/**
 * @author LatvianModder
 */
public class PanelChapters extends Panel
{
	public final GuiQuests treeGui;

	public PanelChapters(Panel panel)
	{
		super(panel);
		treeGui = (GuiQuests) panel.getGui();
		setPosAndSize(0, 1, 20, 0);
	}

	@Override
	public void addWidgets()
	{
		if (Platform.isModLoaded("ftbmoney"))
		{
			add(new ButtonOpenShop(this));
			Color4I borderColor = ThemeProperties.WIDGET_BORDER.get(treeGui.selectedChapter);
			add(new ColorWidget(this, borderColor, null).setPosAndSize(1, 0, width - 2, 1));
		}

		boolean canEdit = treeGui.file.canEdit();

		for (ChapterGroup group : treeGui.file.chapterGroups)
		{
			for (Chapter chapter : group.chapters)
			{
				if (canEdit || chapter.isVisible(treeGui.file.self))
				{
					add(new ButtonChapter(this, chapter));
				}
			}
		}

		if (canEdit)
		{
			add(new ButtonAddChapter(this));
		}
	}

	@Override
	public void alignWidgets()
	{
		setHeight(treeGui.height - 2);
		align(WidgetLayout.VERTICAL);
	}
}