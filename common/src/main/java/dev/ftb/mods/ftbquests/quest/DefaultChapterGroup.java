package dev.ftb.mods.ftbquests.quest;

import net.minecraft.network.chat.Component;

import dev.ftb.mods.ftblibrary.icon.Icon;

public class DefaultChapterGroup extends ChapterGroup {
	public DefaultChapterGroup(BaseQuestFile f) {
		super(0L, f);
	}

	@Override
	public Component getAltTitle() {
		return file.getTitle();
	}

	@Override
	public Icon<?> getAltIcon() {
		return file.getIcon();
	}
}
