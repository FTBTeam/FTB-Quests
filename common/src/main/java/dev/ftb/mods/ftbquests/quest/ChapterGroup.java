package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.gui.quests.QuestScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class ChapterGroup extends QuestObject {
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
	public void fillConfigGroup(ConfigGroup config) {
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
		return Component.literal("Unnamed Group");
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
	public int getRelativeProgressFromChildren(TeamData data) {
		if (chapters.isEmpty()) {
			return 100;
		}

		int progress = 0;

		for (Chapter chapter : chapters) {
			progress += data.getRelativeProgress(chapter);
		}

		return getRelativeProgressFromChildren(progress, chapters.size());
	}

	@Override
	public void onCompleted(QuestProgressEventData<?> data) {
		data.teamData.setCompleted(id, data.time);

		if (file.isCompletedRaw(data.teamData)) {
			file.onCompleted(data.withObject(file));
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

	@Nullable
	public Chapter getFirstVisibleChapter(TeamData data) {
		if (chapters.isEmpty()) {
			return null;
		} else if (file.canEdit()) {
			return chapters.get(0);
		}

		for (Chapter chapter : chapters) {
			if (!chapter.quests.isEmpty() && chapter.isVisible(data)) {
				return chapter;
			}
		}

		return null;
	}

	@Override
	public Collection<? extends QuestObject> getChildren() {
		return chapters;
	}

	@Override
	public boolean hasUnclaimedRewardsRaw(TeamData teamData, UUID player) {
		for (Chapter chapter : chapters) {
			if (teamData.hasUnclaimedRewards(player, chapter)) {
				return true;
			}
		}

		return false;
	}
}
