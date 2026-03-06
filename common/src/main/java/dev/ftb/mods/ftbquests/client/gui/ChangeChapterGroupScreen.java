package dev.ftb.mods.ftbquests.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.screens.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleTextButton;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.net.ChangeChapterGroupMessage;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;

import org.jspecify.annotations.Nullable;

public class ChangeChapterGroupScreen extends AbstractButtonListScreen {
	private final Chapter chapter;
	private final QuestScreen questScreen;
	@Nullable
	private ChapterGroup newGroup;

	public ChangeChapterGroupScreen(Chapter chapter, QuestScreen questScreen) {
		this.chapter = chapter;
		this.questScreen = questScreen;

		setTitle(Component.translatable("ftbquests.gui.change_group"));
		setHasSearchBox(true);
		showCloseButton(true);
		showBottomPanel(false);
		setBorder(1, 1, 1);
	}

	@Override
	public void addButtons(Panel panel) {
		ClientQuestFile.getInstance().getChapterGroups().stream()
				.sorted()
				.forEach(group -> panel.add(new ChapterGroupButton(panel, group)));
	}

	@Override
	protected void doCancel() {
		questScreen.open(chapter, false);
	}

	@Override
	protected void doAccept() {
		if (newGroup != null) {
			NetworkManager.sendToServer(new ChangeChapterGroupMessage(chapter.id, newGroup.id));
		}
		questScreen.open(chapter, false);
	}

	private class ChapterGroupButton extends SimpleTextButton {
		private final ChapterGroup chapterGroup;

		public ChapterGroupButton(Panel panel, ChapterGroup chapterGroup) {
			super(panel, chapterGroup.getTitle(), Color4I.empty());
			this.chapterGroup = chapterGroup;
			setHeight(16);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			newGroup = chapterGroup;
			doAccept();
		}

		@Override
		public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			if (isMouseOver) {
				IconHelper.renderIcon(Color4I.WHITE.withAlpha(30), graphics, x, y, w, h);
			}
			IconHelper.renderIcon(Color4I.GRAY.withAlpha(40), graphics, x, y + h, w, 1);
		}
	}
}
