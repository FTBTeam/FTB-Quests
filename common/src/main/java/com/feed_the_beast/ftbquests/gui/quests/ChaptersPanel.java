package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;

/**
 * @author LatvianModder
 */
public class ChaptersPanel extends Panel {
	public static final Icon ARROW_RIGHT = Icon.getIcon("ftbquests:textures/gui/arrow_right.png");
	public static final Icon ARROW_DOWN = Icon.getIcon("ftbquests:textures/gui/arrow_down.png");

	public final QuestsScreen questsScreen;
	public boolean expanded = false;

	public ChaptersPanel(Panel panel) {
		super(panel);
		questsScreen = (QuestsScreen) panel.getGui();
	}

	@Override
	public void addWidgets() {

		/*
		if (Platform.isModLoaded("ftbmoney")) {
			add(new OpenShopButton(this));
			Color4I borderColor = ThemeProperties.WIDGET_BORDER.get(treeGui.selectedChapter);
			add(new ColorWidget(this, borderColor, null).setPosAndSize(1, 0, width - 2, 1));
		}
		 */

		boolean canEdit = questsScreen.file.canEdit();

		/*
		if (treeGui.file.chapterGroups.size() == 1) {
			for (Chapter chapter : treeGui.file.defaultChapterGroup.getVisibleChapters(treeGui.file.self)) {
				add(new ChapterButton(this, chapter));
			}
		} else {
			for (ChapterGroup group : treeGui.file.chapterGroups) {
				List<Chapter> visibleChapters = group.getVisibleChapters(treeGui.file.self);

				for (Chapter chapter : visibleChapters) {
					add(new ChapterButton(this, chapter));
				}

				//add(new ButtonExpandedChapter(this, chapter));
			}
		}
		 */

		if (canEdit) {
			//add(new ExpandChaptersButton(this));
		}
	}

	@Override
	public void alignWidgets() {
		if (expanded) {
			setPosAndSize(0, 0, 100, questsScreen.height);
		} else {
			setPosAndSize(-1, 0, 0, 0);
		}
	}

	@Override
	public void updateMouseOver(int mouseX, int mouseY) {
		super.updateMouseOver(mouseX, mouseY);

		if (expanded && !isMouseOver()) {
			expanded = false;
			refreshWidgets();
		}
	}
}