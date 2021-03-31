package dev.ftb.mods.ftbquests.quest;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.IconAnimation;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import dev.ftb.mods.ftbquests.gui.quests.QuestScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ChapterGroup extends QuestObjectBase {
	public final QuestFile file;
	public final List<Chapter> chapters;

	public boolean guiCollapsed;

	public ChapterGroup(QuestFile f) {
		file = f;
		chapters = new ArrayList<>();

		guiCollapsed = false;
	}

	@Override
	public QuestObjectType getObjectType() {
		return QuestObjectType.CHAPTER_GROUP;
	}

	@Override
	public QuestFile getQuestFile() {
		return file;
	}

	public int getIndex() {
		return file.chapterGroups.indexOf(this);
	}

	public boolean isDefaultGroup() {
		return this == file.defaultChapterGroup;
	}

	@Override
	public void onCreated() {
		file.chapterGroups.add(this);
	}

	@Override
	public void clearCachedData() {
		super.clearCachedData();

		for (Chapter chapter : chapters) {
			chapter.clearCachedData();
		}
	}

	@Override
	public void deleteSelf() {
		file.chapterGroups.remove(this);

		for (Chapter chapter : chapters) {
			chapter.group = file.defaultChapterGroup;
			file.defaultChapterGroup.chapters.add(chapter);
		}

		super.deleteSelf();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		config.addString("title", title, v -> title = v, "").setNameKey("ftbquests.title").setOrder(-127);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void editedFromGUI() {
		QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);

		if (gui != null) {
			gui.chapterPanel.refreshWidgets();
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Component getAltTitle() {
		return new TextComponent("Unnamed Group");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		List<Icon> list = new ArrayList<>();

		for (Chapter chapter : chapters) {
			list.add(chapter.getIcon());
		}

		return IconAnimation.fromList(list, false);
	}

	public boolean isVisible(TeamData data) {
		for (Chapter chapter : chapters) {
			if (chapter.isVisible(data)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void changeProgress(TeamData data, ChangeProgress type) {
		for (Chapter chapter : chapters) {
			chapter.changeProgress(data, type);
		}
	}

	public List<Chapter> getVisibleChapters(TeamData data) {
		if (file.canEdit()) {
			return chapters;
		}

		List<Chapter> list = new ArrayList<>();

		for (Chapter chapter : chapters) {
			if (!chapter.quests.isEmpty() && chapter.isVisible(data)) {
				list.add(chapter);
			}
		}

		return list;
	}
}
