package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import dev.ftb.mods.ftblibrary.math.Bits;
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
import java.util.regex.Pattern;

public final class Chapter extends QuestObject {
	private static final Pattern HEX_STRING = Pattern.compile("^([a-fA-F0-9]+)?$");

	public final BaseQuestFile file;

	private ChapterGroup group;
	private String filename;
	private final List<Quest> quests;
	private final List<QuestLink> questLinks;
	private final List<String> rawSubtitle;
	private boolean alwaysInvisible;
	private String defaultQuestShape;
	private double defaultQuestSize;
	private final List<ChapterImage> images;
	boolean defaultHideDependencyLines;
	private int defaultMinWidth = 0;
	private ProgressionMode progressionMode;
	private boolean hideQuestDetailsUntilStartable;
	private boolean hideQuestUntilDepsComplete;
	private boolean hideQuestUntilDepsVisible;
	private boolean defaultRepeatable;
	private Tristate consumeItems;
	private boolean requireSequentialTasks;
	private String autoFocusId;

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
		defaultQuestSize = 1D;
		images = new ArrayList<>();
		defaultHideDependencyLines = false;
		progressionMode = ProgressionMode.DEFAULT;
		hideQuestUntilDepsVisible = false;
		hideQuestUntilDepsComplete = false;
		hideQuestDetailsUntilStartable = false;
		defaultRepeatable = false;
		consumeItems = Tristate.DEFAULT;
		requireSequentialTasks = false;
		autoFocusId = "";
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

	public boolean isDefaultRepeatable() {
		return defaultRepeatable;
	}

	public boolean isRequireSequentialTasks() {
		return requireSequentialTasks;
	}

	public List<Quest> getQuests() {
		return Collections.unmodifiableList(quests);
	}

	public List<QuestLink> getQuestLinks() {
		return Collections.unmodifiableList(questLinks);
	}

	public List<ChapterImage> getImages() {
		return Collections.unmodifiableList(images);
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
		if (defaultQuestSize != 1D) {
			nbt.putDouble("default_quest_size", defaultQuestSize);
		}
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

		consumeItems.write(nbt, "consume_items");

		if (hideQuestDetailsUntilStartable) nbt.putBoolean("hide_quest_details_until_startable", true);
		if (hideQuestUntilDepsVisible) nbt.putBoolean("hide_quest_until_deps_visible", true);
		if (hideQuestUntilDepsComplete) nbt.putBoolean("hide_quest_until_deps_complete", true);
		if (defaultRepeatable) nbt.putBoolean("default_repeatable_quest", true);
		if (requireSequentialTasks) nbt.putBoolean("require_sequential_tasks", true);

		if (!autoFocusId.isEmpty()) nbt.putString("autofocus_id", autoFocusId);
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

		defaultQuestSize = nbt.contains("default_quest_size", SNBTCompoundTag.TAG_DOUBLE) ?
				nbt.getDouble("default_quest_size") :
				1D;

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
		consumeItems = Tristate.read(nbt, "consume_items");
		hideQuestDetailsUntilStartable = nbt.getBoolean("hide_quest_details_until_startable");
		hideQuestUntilDepsVisible = nbt.getBoolean("hide_quest_until_deps_visible");
		hideQuestUntilDepsComplete = nbt.getBoolean("hide_quest_until_deps_complete");
		defaultRepeatable = nbt.getBoolean("default_repeatable_quest");
		requireSequentialTasks = nbt.getBoolean("require_sequential_tasks");
		autoFocusId = nbt.getString("autofocus_id");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(filename, Short.MAX_VALUE);
		NetUtils.writeStrings(buffer, rawSubtitle);
		buffer.writeUtf(defaultQuestShape, Short.MAX_VALUE);
		buffer.writeDouble(defaultQuestSize);
		NetUtils.write(buffer, images, (d, img) -> img.writeNetData(d));
		buffer.writeInt(defaultMinWidth);
		ProgressionMode.NAME_MAP.write(buffer, progressionMode);

		int flags = 0;
		flags = Bits.setFlag(flags, 0x01, alwaysInvisible);
		flags = Bits.setFlag(flags, 0x02, defaultHideDependencyLines);
		flags = Bits.setFlag(flags, 0x04, hideQuestDetailsUntilStartable);
		flags = Bits.setFlag(flags, 0x08, hideQuestUntilDepsComplete);
		flags = Bits.setFlag(flags, 0x10, defaultRepeatable);
		flags = Bits.setFlag(flags, 0x20, consumeItems != Tristate.DEFAULT);
		flags = Bits.setFlag(flags, 0x40, consumeItems == Tristate.TRUE);
		flags = Bits.setFlag(flags, 0x80, requireSequentialTasks);
		flags = Bits.setFlag(flags, 0x100, !autoFocusId.isEmpty());
		flags = Bits.setFlag(flags, 0x200, hideQuestUntilDepsVisible);
		buffer.writeVarInt(flags);

		if (!autoFocusId.isEmpty()) buffer.writeLong(QuestObjectBase.parseHexId(autoFocusId).orElse(0L));
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		filename = buffer.readUtf(Short.MAX_VALUE);
		NetUtils.readStrings(buffer, rawSubtitle);
		defaultQuestShape = buffer.readUtf(Short.MAX_VALUE);
		defaultQuestSize = buffer.readDouble();
		NetUtils.read(buffer, images, d -> {
			ChapterImage image = new ChapterImage(this);
			image.readNetData(d);
			return image;
		});
		defaultMinWidth = buffer.readInt();
		progressionMode = ProgressionMode.NAME_MAP.read(buffer);

		int flags = buffer.readVarInt();
		alwaysInvisible = Bits.getFlag(flags, 0x01);
		defaultHideDependencyLines = Bits.getFlag(flags, 0x02);
		hideQuestDetailsUntilStartable = Bits.getFlag(flags, 0x04);
		hideQuestUntilDepsComplete = Bits.getFlag(flags, 0x08);
		defaultRepeatable = Bits.getFlag(flags, 0x10);
		consumeItems = Bits.getFlag(flags, 0x20) ? Bits.getFlag(flags, 0x40) ? Tristate.TRUE : Tristate.FALSE : Tristate.DEFAULT;
		requireSequentialTasks = Bits.getFlag(flags, 0x80);
		hideQuestUntilDepsVisible = Bits.getFlag(flags, 0x200);

		autoFocusId = Bits.getFlag(flags, 0x100) ? QuestObjectBase.getCodeString(buffer.readLong()) : "";
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
			if (!quest.isProgressionIgnored(data)) {
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

		ConfigGroup appearance = config.getOrCreateSubgroup("appearance").setNameKey("ftbquests.quest.appearance");
		appearance.addEnum("default_quest_shape", defaultQuestShape.isEmpty() ? "default" : defaultQuestShape, v -> defaultQuestShape = v.equals("default") ? "" : v, QuestShape.idMapWithDefault);
		appearance.addDouble("default_quest_size", defaultQuestSize, v -> defaultQuestSize = v, 1, 0.0625D, 8D);
		appearance.addInt("default_min_width", defaultMinWidth, v -> defaultMinWidth = v, 0, 0, 3000);

		ConfigGroup visibility = config.getOrCreateSubgroup("visibility").setNameKey("ftbquests.quest.visibility");
		visibility.addBool("always_invisible", alwaysInvisible, v -> alwaysInvisible = v, false);
		visibility.addBool("default_hide_dependency_lines", defaultHideDependencyLines, v -> defaultHideDependencyLines = v, false);
		visibility.addBool("hide_quest_details_until_startable", hideQuestDetailsUntilStartable, v -> hideQuestDetailsUntilStartable = v, false);
		visibility.addBool("hide_quest_until_deps_visible", hideQuestUntilDepsVisible, v -> hideQuestUntilDepsVisible = v, false);
		visibility.addBool("hide_quest_until_deps_complete", hideQuestUntilDepsComplete, v -> hideQuestUntilDepsComplete = v, false);

		ConfigGroup misc = config.getOrCreateSubgroup("misc").setNameKey("ftbquests.quest.misc");
		misc.addString("autofocus_id", autoFocusId, v -> autoFocusId = v, "", HEX_STRING);
		misc.addEnum("progression_mode", progressionMode, v -> progressionMode = v, ProgressionMode.NAME_MAP);
		misc.addBool("default_repeatable", defaultRepeatable, v -> defaultRepeatable = v, false);
		misc.addTristate("consume_items", consumeItems, v -> consumeItems = v);
		misc.addBool("require_sequential_tasks", requireSequentialTasks, v -> requireSequentialTasks = v, false);
	}

	@Override
	public boolean isVisible(TeamData data) {
		return !alwaysInvisible &&
				(quests.isEmpty() && questLinks.isEmpty() ||
						quests.stream().anyMatch(quest -> quest.isVisible(data)) ||
						questLinks.stream().anyMatch(link -> link.isVisible(data)));
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

	public boolean hideQuestUntilDepsComplete() {
		return hideQuestUntilDepsComplete;
	}

	public boolean isHideQuestUntilDepsVisible() {
		return hideQuestUntilDepsVisible;
	}

	public void addImage(ChapterImage image) {
		images.add(image);
	}

	public void removeImage(ChapterImage image) {
		images.remove(image);
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

	public boolean consumeItems() {
		return consumeItems.get(file.isDefaultTeamConsumeItems());
	}

	public double getDefaultQuestSize() {
		return defaultQuestSize;
	}

	public boolean hasAnyVisibleChildren() {
		return !quests.isEmpty() || !questLinks.isEmpty();
	}

	public Optional<Movable> getAutofocus() {
		if (autoFocusId != null && !autoFocusId.isEmpty()) {
			return QuestObjectBase.parseHexId(autoFocusId)
					.flatMap(id -> file.get(id) instanceof Movable m && m.getChapter() == this ?
							Optional.of(m) :
							Optional.empty()
					);
		}
		return Optional.empty();
	}

	public void setAutofocus(long id) {
		autoFocusId = id == 0L ? "" : QuestObjectBase.getCodeString(id);
	}

	public boolean isAutofocus(long id) {
		return id == getAutofocus().map(Movable::getMovableID).orElse(0L);
	}
}
