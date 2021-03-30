package dev.ftb.mods.ftbquests.quest;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

/**
 * @author LatvianModder
 */
public class DefaultChapterGroup extends ChapterGroup {
	public DefaultChapterGroup(QuestFile f) {
		super(f);
	}

	@Override
	public String toString() {
		return "-";
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
