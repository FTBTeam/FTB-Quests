package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.network.chat.Component;

public class DefaultChapterGroup extends ChapterGroup {
	public DefaultChapterGroup(BaseQuestFile f) {
		super(0L, f);
	}

	@Override
	public Component getAltTitle() {
		return file.getTitle();
	}

	@Override
	public Icon getAltIcon() {
		return file.getIcon();
	}
}
