package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.icon.Icon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

public class DefaultChapterGroup extends ChapterGroup {
	public DefaultChapterGroup(QuestFile f) {
		super(0L, f);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Component getAltTitle() {
		return file.getTitle();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		return file.getIcon();
	}
}
