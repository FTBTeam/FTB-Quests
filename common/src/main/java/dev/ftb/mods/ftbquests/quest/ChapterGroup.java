package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.icon.AnimatedIcon;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.events.progress.ProgressEventData;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class ChapterGroup extends QuestObject {
	protected final BaseQuestFile file;
	private final List<Chapter> chapters;

	private boolean guiCollapsed;

	public ChapterGroup(long id, BaseQuestFile file) {
		super(id);

		this.file = file;
		chapters = new ArrayList<>();
		guiCollapsed = false;
	}

	public BaseQuestFile getFile() {
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
	public BaseQuestFile getQuestFile() {
		return file;
	}

	public boolean isFirstGroup() {
		return !file.chapterGroups.isEmpty() && this == file.chapterGroups.getFirst();
	}

	public boolean isLastGroup() {
		return !file.chapterGroups.isEmpty() && this == file.chapterGroups.getLast();
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
	public void fillConfigGroup(EditableConfigGroup config) {
		config.addString("title", getRawTitle(), this::setRawTitle, "").setNameKey("ftbquests.title").setOrder(-127);
	}

	@Override
	public void editedFromGUI() {
		QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);

		if (gui != null) {
			gui.refreshChapterPanel();
		}
	}

	@Override
	public Component getAltTitle() {
		return Component.literal("Unnamed Group");
	}

	@Override
	public Icon<?> getAltIcon() {
		List<Icon<?>> list = new ArrayList<>();

		for (Chapter chapter : chapters) {
			list.add(chapter.getIcon());
		}

		return AnimatedIcon.fromList(list, false);
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
	public void onCompleted(ProgressEventData<?> data) {
		data.setCompleted(id);

		if (file.isCompletedRaw(data.teamData())) {
			file.onCompleted(data.withObject(file));
		}
	}

	public List<Chapter> getVisibleChapters(TeamData data) {
		return file.canEdit() ? chapters : chapters.stream()
						.filter(chapter -> chapter.hasAnyVisibleChildren() && chapter.isVisible(data))
						.toList();
	}

	@Nullable
	public Chapter getFirstVisibleChapter(TeamData data) {
		if (chapters.isEmpty()) {
			return null;
		} else if (file.canEdit()) {
			return chapters.getFirst();
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
