package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author LatvianModder
 */
public class ChapterGroup extends QuestObject {
	protected final QuestFile file;
	private final List<Chapter> chapters;

	private boolean guiCollapsed;

	public ChapterGroup(long id, QuestFile file) {
		super(id);

		this.file = file;
		chapters = new ArrayList<>();
		guiCollapsed = false;
	}

	public QuestFile getFile() {
		return file;
	}

	public List<Chapter> getChapters() {
		return Collections.unmodifiableList(chapters);
	}

	public void addChapter(Chapter chapter) {
		chapters.add(chapter);
		chapter.setGroup(this);
	}
	
	public void removeChapter(Chapter chapter) {
		chapters.remove(chapter);
	}

	public void clearChapters() {
		chapters.clear();
	}

	public void sortChapters(Comparator<? super Chapter> c) {
		chapters.sort(c);
	}

	@Override
	public QuestObjectType getObjectType() {
		return QuestObjectType.CHAPTER_GROUP;
	}

	@Override
	public QuestFile getQuestFile() {
		return file;
	}

	public boolean isFirstGroup() {
		return !file.chapterGroups.isEmpty() && this == file.chapterGroups.get(0);
	}

	public boolean isLastGroup() {
		return !file.chapterGroups.isEmpty() && this == file.chapterGroups.get(file.chapterGroups.size() - 1);
	}

	public boolean isDefaultGroup() {
		return this == file.getDefaultChapterGroup();
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
			file.getDefaultChapterGroup().addChapter(chapter);
		}

		super.deleteSelf();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		config.addString("title", rawTitle, v -> rawTitle = v, "").setNameKey("ftbquests.title").setOrder(-127);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void editedFromGUI() {
		QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);

		if (gui != null) {
			gui.refreshChapterPanel();
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
		return chapters.stream().anyMatch(chapter -> chapter.isVisible(data));
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
		data.setCompleted(id);

		if (file.isCompletedRaw(data.getTeamData())) {
			file.onCompleted(data.withObject(file));
		}
	}

	public List<Chapter> getVisibleChapters(TeamData data) {
		if (file.canEdit()) {
			return chapters;
		}

		List<Chapter> list = new ArrayList<>();

		for (Chapter chapter : chapters) {
			if (!chapter.getQuests().isEmpty() && chapter.isVisible(data)) {
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

		return chapters.stream()
				.filter(chapter -> !chapter.getQuests().isEmpty() && chapter.isVisible(data))
				.findFirst()
				.orElse(null);

	}

	@Override
	public Collection<? extends QuestObject> getChildren() {
		return chapters;
	}

	@Override
	public boolean hasUnclaimedRewardsRaw(TeamData teamData, UUID player) {
		return chapters.stream().anyMatch(chapter -> teamData.hasUnclaimedRewards(player, chapter));
	}

	public boolean moveChapterWithinGroup(Chapter chapter, boolean movingUp) {
		int index = chapters.indexOf(chapter);

		if (index != -1 && movingUp ? (index > 0) : (index < chapters.size() - 1)) {
			chapters.remove(index);
			chapters.add(movingUp ? index - 1 : index + 1, chapter);
			return true;
		}
		return false;
	}

	public void toggleCollapsed() {
		guiCollapsed = !guiCollapsed;
	}

	public boolean isGuiCollapsed() {
		return guiCollapsed;
	}
}
