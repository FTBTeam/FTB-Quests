package dev.ftb.mods.ftbquests.client.gui;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.ButtonListBaseScreen;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.net.ChangeChapterGroupMessage;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import net.minecraft.network.chat.Component;

public class ChangeChapterGroupScreen extends ButtonListBaseScreen {
	private final Chapter chapter;
	private final QuestScreen questScreen;

	public ChangeChapterGroupScreen(Chapter chapter, QuestScreen questScreen) {
		this.chapter = chapter;
		this.questScreen = questScreen;

		setTitle(Component.translatable("ftbquests.gui.change_group"));
		setHasSearchBox(true);
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel) {
		ClientQuestFile.INSTANCE.forAllChapterGroups(group -> panel.add(new ChapterGroupButton(panel, group)));
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}

	private class ChapterGroupButton extends SimpleTextButton {
		private final ChapterGroup chapterGroup;

		public ChapterGroupButton(Panel panel, ChapterGroup chapterGroup) {
			super(panel, chapterGroup.getTitle(), Color4I.empty());
			this.chapterGroup = chapterGroup;
			setHeight(14);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			new ChangeChapterGroupMessage(chapter.id, chapterGroup.id).sendToServer();
			questScreen.open(chapter, false);
		}
	}
}
