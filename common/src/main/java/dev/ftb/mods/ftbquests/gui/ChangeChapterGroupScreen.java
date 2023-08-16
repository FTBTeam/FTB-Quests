package dev.ftb.mods.ftbquests.gui;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.ButtonListBaseScreen;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.net.ChangeChapterGroupMessage;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import net.minecraft.network.chat.Component;

public class ChangeChapterGroupScreen extends ButtonListBaseScreen {
	private class ChapterGroupButton extends SimpleTextButton {
		private final ChapterGroup chapterGroup;

		public ChapterGroupButton(Panel panel, ChapterGroup t) {
			super(panel, t.getTitle(), Color4I.EMPTY);
			chapterGroup = t;
			setHeight(14);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			new ChangeChapterGroupMessage(chapter.id, chapterGroup.id).sendToServer();
			ClientQuestFile.INSTANCE.questScreen.open(chapter, false);
		}
	}

	private final Chapter chapter;

	public ChangeChapterGroupScreen(Chapter c) {
		chapter = c;
		setTitle(Component.translatable("ftbquests.gui.change_group"));
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
