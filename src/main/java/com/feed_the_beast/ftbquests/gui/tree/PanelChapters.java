package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.ColorWidget;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import net.minecraftforge.fml.common.Loader;

/**
 * @author LatvianModder
 */
public class PanelChapters extends Panel
{
	public final GuiQuestTree treeGui;

	public PanelChapters(Panel panel)
	{
		super(panel);
		treeGui = (GuiQuestTree) panel.getGui();
		setPosAndSize(0, 1, 20, 0);
	}

	@Override
	public void addWidgets()
	{
		if (Loader.isModLoaded("ftbmoney"))
		{
			add(new ButtonOpenShop(this));
			add(new ColorWidget(this, treeGui.borderColor, null).setPosAndSize(1, 0, width - 2, 1));
		}

		boolean canEdit = treeGui.file.canEdit();

		for (QuestChapter chapter : treeGui.file.chapters)
		{
			if ((canEdit || chapter.isVisible(treeGui.file.self)) && (chapter.group == null || chapter.group.invalid))
			{
				add(new ButtonChapter(this, chapter));
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