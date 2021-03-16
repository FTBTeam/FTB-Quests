package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.MessageChangeChapterGroup;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.ChapterGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.misc.GuiButtonListBase;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author LatvianModder
 */
public class ChangeChapterGroupScreen extends GuiButtonListBase {
	private class ChapterGroupButton extends SimpleTextButton {
		private final ChapterGroup chapterGroup;

		public ChapterGroupButton(Panel panel, ChapterGroup t) {
			super(panel, t.getTitle(), Icon.EMPTY);
			chapterGroup = t;
			setHeight(14);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			new MessageChangeChapterGroup(chapter.id, chapterGroup.id).sendToServer();
			ClientQuestFile.INSTANCE.questScreen.open(chapter, false);
		}
	}

	private final Chapter chapter;

	public ChangeChapterGroupScreen(Chapter c) {
		chapter = c;
		setTitle(new TranslatableComponent("ftbquests.gui.change_group"));
		setHasSearchBox(true);
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel) {
		for (ChapterGroup group : ClientQuestFile.INSTANCE.chapterGroups) {
			panel.add(new ChapterGroupButton(panel, group));
		}
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}
}