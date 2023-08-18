package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.ftb.mods.ftbquests.events.ObjectStartedEvent;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;
import java.util.stream.Stream;

public final class Chapter extends QuestObject {
	public final BaseQuestFile file;

	private ChapterGroup group;
	private String filename;
	private final List<Quest> quests;
	private final List<QuestLink> questLinks;
	private final List<String> rawSubtitle;
	boolean alwaysInvisible;
	private String defaultQuestShape;
	private final List<ChapterImage> images;
	boolean defaultHideDependencyLines;
	private int defaultMinWidth = 0;
	private ProgressionMode progressionMode;
	private boolean hideQuestDetailsUntilStartable;
	private boolean hideQuestUntilDepsVisible;

	public Chapter(long id, BaseQuestFile file, ChapterGroup group) {
		this(id, file, group, "");
	}

	public Chapter(long id, BaseQuestFile file, ChapterGroup group, String filename) {
		super(id);

		this.file = file;
		this.group = group;
		this.filename = filename;
		quests = new ArrayList<>();
		questLinks = new ArrayList<>();
		rawSubtitle = new ArrayList<>(0);
		alwaysInvisible = false;
		defaultQuestShape = "";
		images = new ArrayList<>();
		defaultHideDependencyLines = false;
		progressionMode = ProgressionMode.DEFAULT;
		hideQuestUntilDepsVisible = false;
		hideQuestDetailsUntilStartable = false;
	}

	public void setDefaultQuestShape(String defaultQuestShape) {
		this.defaultQuestShape = defaultQuestShape;
	}

	public ChapterGroup getGroup() {
		return group;
	}

	void setGroup(ChapterGroup group) {
		this.group = group;
	}

	@Override
	public QuestObjectType getObjectType() {
		return QuestObjectType.CHAPTER;
	}

	@Override
	public BaseQuestFile getQuestFile() {
		return group.getFile();
	}

	@Override
	public Chapter getQuestChapter() {
		return this;
	}

	public int getDefaultMinWidth() {
		return defaultMinWidth;
	}

	public boolean isAlwaysInvisible() {
		return alwaysInvisible;
	}

	public List<Quest> getQuests() {
		return Collections.unmodifiableList(quests);
	}

	public List<QuestLink> getQuestLinks() {
		return Collections.unmodifiableList(questLinks);
	}

	public void addQuest(Quest quest) {
		quests.add(quest);
	}

	public void removeQuest(Quest quest) {
		quests.remove(quest);
	}

	@Override
	public void writeData(CompoundTag nbt) {
		nbt.putString("filename", filename);
		super.writeData(nbt);

		if (!rawSubtitle.isEmpty()) {
			ListTag list = new ListTag();

			for (String v : rawSubtitle) {
				list.add(StringTag.valueOf(v));
			}

			nbt.put("subtitle", list);
		}

		if (alwaysInvisible) {
			nbt.putBoolean("always_invisible", true);
		}

		nbt.putString("default_quest_shape", defaultQuestShape);
		nbt.putBoolean("default_hide_dependency_lines", defaultHideDependencyLines);

		if (!images.isEmpty()) {
			ListTag list = new ListTag();

			for (ChapterImage image : images) {
				SNBTCompoundTag nbt1 = new SNBTCompoundTag();
				image.writeData(nbt1);
				list.add(nbt1);
			}

			nbt.put("images", list);
		}

		if (defaultMinWidth > 0) {
			nbt.putInt("default_min_width", defaultMinWidth);
		}

		if (progressionMode != ProgressionMode.DEFAULT) {
			nbt.putString("progression_mode", progressionMode.getId());
		}

		if (hideQuestDetailsUntilStartable) nbt.putBoolean("hide_quest_details_until_startable", true);
		if (hideQuestUntilDepsVisible) nbt.putBoolean("hide_quest_until_deps_visible", true);
	}

	@Override
	public void readData(CompoundTag nbt) {
		filename = nbt.getString("filename");
		super.readData(nbt);
		rawSubtitle.clear();

		ListTag subtitleNBT = nbt.getList("subtitle", Tag.TAG_STRING);

		for (int i = 0; i < subtitleNBT.size(); i++) {
			rawSubtitle.add(subtitleNBT.getString(i));
		}

		alwaysInvisible = nbt.getBoolean("always_invisible");
		defaultQuestShape = nbt.getString("default_quest_shape");

		if (defaultQuestShape.equals("default")) {
			defaultQuestShape = "";
		}

		defaultHideDependencyLines = nbt.getBoolean("default_hide_dependency_lines");

		ListTag imgs = nbt.getList("images", Tag.TAG_COMPOUND);

		images.clear();

		for (int i = 0; i < imgs.size(); i++) {
			ChapterImage image = new ChapterImage(this);
			image.readData(imgs.getCompound(i));
			images.add(image);
		}

		defaultMinWidth = nbt.getInt("default_min_width");
		progressionMode = ProgressionMode.NAME_MAP.get(nbt.getString("progression_mode"));
		hideQuestDetailsUntilStartable = nbt.getBoolean("hide_quest_details_until_startable");
		hideQuestUntilDepsVisible = nbt.getBoolean("hide_quest_until_deps_visible");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(filename, Short.MAX_VALUE);
		NetUtils.writeStrings(buffer, rawSubtitle);
		buffer.writeBoolean(alwaysInvisible);
		buffer.writeUtf(defaultQuestShape, Short.MAX_VALUE);
		NetUtils.write(buffer, images, (d, img) -> img.writeNetData(d));
		buffer.writeBoolean(defaultHideDependencyLines);
		buffer.writeInt(defaultMinWidth);
		ProgressionMode.NAME_MAP.write(buffer, progressionMode);
		buffer.writeBoolean(hideQuestDetailsUntilStartable);
		buffer.writeBoolean(hideQuestUntilDepsVisible);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		filename = buffer.readUtf(Short.MAX_VALUE);
		NetUtils.readStrings(buffer, rawSubtitle);
		alwaysInvisible = buffer.readBoolean();
		defaultQuestShape = buffer.readUtf(Short.MAX_VALUE);
		NetUtils.read(buffer, images, d -> {
			ChapterImage image = new ChapterImage(this);
			image.readNetData(d);
			return image;
		});
		defaultHideDependencyLines = buffer.readBoolean();
		defaultMinWidth = buffer.readInt();
		progressionMode = ProgressionMode.NAME_MAP.read(buffer);
		hideQuestDetailsUntilStartable = buffer.readBoolean();
		hideQuestUntilDepsVisible = buffer.readBoolean();
	}

	public int getIndex() {
		return group.getChapters().indexOf(this);
	}

	@Override
	public int getRelativeProgressFromChildren(TeamData data) {
		if (alwaysInvisible) {
			return 100;
		}

		if (quests.isEmpty()) {
			return 100;
		}

		int progress = 0;
		int count = 0;

		for (Quest quest : quests) {
			if (!quest.isProgressionIgnored()) {
				progress += data.getRelativeProgress(quest);
				count++;
			}
		}

		if (count <= 0) {
			return 100;
		}

		return getRelativeProgressFromChildren(progress, count);
	}

	@Override
	public void onStarted(QuestProgressEventData<?> data) {
		data.setStarted(id);
		ObjectStartedEvent.CHAPTER.invoker().act(new ObjectStartedEvent.ChapterEvent(data.withObject(this)));

		if (!data.getTeamData().isStarted(file)) {
			file.onStarted(data.withObject(file));
		}
	}

	@Override
	public void onCompleted(QuestProgressEventData<?> data) {
		data.setCompleted(id);
		ObjectCompletedEvent.CHAPTER.invoker().act(new ObjectCompletedEvent.ChapterEvent(data.withObject(this)));

		if (!disableToast) {
			data.notifyPlayers(id);
		}

		file.forAllQuests(quest -> {
			if (quest.hasDependency(this)) {
				data.getTeamData().checkAutoCompletion(quest);
			}
		});

		if (group.isCompletedRaw(data.getTeamData())) {
			group.onCompleted(data.withObject(group));
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.unnamed");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		List<Icon> list = new ArrayList<>();

		for (Quest quest : quests) {
			list.add(quest.getIcon());
		}

		return IconAnimation.fromList(list, false);
	}

	@Override
	public void deleteSelf() {
		super.deleteSelf();
		group.removeChapter(this);
	}

	@Override
	public void deleteChildren() {
		for (Quest quest : quests) {
			quest.deleteChildren();
			quest.invalid = true;
		}

		quests.clear();
	}

	@Override
	public void onCreated() {
		if (filename.isEmpty()) {
			String basename = titleToID(rawTitle).orElse(toString());
			filename = basename;

			Set<String> existingNames = new HashSet<>();
			getQuestFile().forAllChapters(ch -> existingNames.add(ch.filename));

			for (int i = 2; existingNames.contains(filename); i++) {
				filename = basename + "_" + i;
			}
		}

		group.addChapter(this);

		if (!quests.isEmpty()) {
			List<Quest> l = new ArrayList<>(quests);
			quests.clear();
			for (Quest quest : l) {
				quest.onCreated();
			}
		}
	}

	public String getFilename() {
		if (filename.isEmpty()) {
			filename = getCodeString(this);
		}

		return filename;
	}

	@Override
	public Optional<String> getPath() {
		return Optional.of("chapters/" + getFilename() + ".snbt");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addList("subtitle", rawSubtitle, new StringConfig(null), "");
		config.addBool("always_invisible", alwaysInvisible, v -> alwaysInvisible = v, false);
		config.addEnum("default_quest_shape", defaultQuestShape.isEmpty() ? "default" : defaultQuestShape, v -> defaultQuestShape = v.equals("default") ? "" : v, QuestShape.idMapWithDefault);
		config.addBool("default_hide_dependency_lines", defaultHideDependencyLines, v -> defaultHideDependencyLines = v, false);
		config.addInt("default_min_width", defaultMinWidth, v -> defaultMinWidth = v, 0, 0, 3000);
		config.addEnum("progression_mode", progressionMode, v -> progressionMode = v, ProgressionMode.NAME_MAP);
		config.addBool("hide_quest_details_until_startable", hideQuestDetailsUntilStartable, v -> hideQuestDetailsUntilStartable = v, false);
		config.addBool("hide_quest_until_deps_visible", hideQuestUntilDepsVisible, v -> hideQuestUntilDepsVisible = v, false);
	}

	@Override
	public boolean isVisible(TeamData data) {
		return !alwaysInvisible && quests.stream().anyMatch(quest -> quest.isVisible(data));
	}

	@Override
	public void clearCachedData() {
		super.clearCachedData();

		for (Quest quest : quests) {
			quest.clearCachedData();
		}
	}

	@Override
	protected void verifyDependenciesInternal(long original, int depth) {
		if (depth >= 1000) {
			throw new DependencyDepthException(this);
		}

		for (Quest quest : quests) {
			if (quest.id == original) {
				throw new DependencyLoopException(this);
			}

			quest.verifyDependenciesInternal(original, depth + 1);
		}
	}

	public boolean hasGroup() {
		return !group.isDefaultGroup();
	}

	public String getDefaultQuestShape() {
		return defaultQuestShape.isEmpty() ? file.getDefaultQuestShape() : defaultQuestShape;
	}

	@Override
	public Collection<? extends QuestObject> getChildren() {
		return quests;
	}

	@Override
	public boolean hasUnclaimedRewardsRaw(TeamData teamData, UUID player) {
		for (Quest quest : quests) {
			if (teamData.hasUnclaimedRewards(player, quest)) {
				return true;
			}
		}

		return false;
	}

	public ProgressionMode getProgressionMode() {
		return progressionMode == ProgressionMode.DEFAULT ? file.getProgressionMode() : progressionMode;
	}

	public boolean hideQuestDetailsUntilStartable() {
		return hideQuestDetailsUntilStartable;
	}

	public boolean hideQuestUntilDepsVisible() {
		return hideQuestUntilDepsVisible;
	}

	public void addImage(ChapterImage image) {
		images.add(image);
	}

	public void removeImage(ChapterImage image) {
		images.remove(image);
	}

	public Stream<ChapterImage> images() {
		return images.stream();
	}

	public void addQuestLink(QuestLink link) {
		questLinks.add(link);
	}

	public void removeQuestLink(QuestLink link) {
		questLinks.remove(link);
	}

	public List<String> getRawSubtitle() {
		return Collections.unmodifiableList(rawSubtitle);
	}
}
