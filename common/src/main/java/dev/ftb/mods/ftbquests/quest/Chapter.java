package dev.ftb.mods.ftbquests.quest;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.Tristate;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableString;
import dev.ftb.mods.ftblibrary.icon.AnimatedIcon;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.platform.event.NativeEventPosting;
import dev.ftb.mods.ftbquests.api.event.progress.ChapterProgressEvent;
import dev.ftb.mods.ftbquests.api.event.progress.ProgressEventData;
import dev.ftb.mods.ftbquests.api.event.progress.ProgressType;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.regex.Pattern;

public final class Chapter extends QuestObject {
	private static final Pattern HEX_STRING = Pattern.compile("^([a-fA-F0-9]+)?$");

	public final BaseQuestFile file;

	private ChapterGroup group;
	private String filename;
	private final List<Quest> quests;
	private final List<QuestLink> questLinks;
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
	private boolean hideTextUntilComplete;
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

	public boolean isHideTextUntilComplete() {
		return hideTextUntilComplete;
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

	public void addChildObject(Movable child) {
        switch (child) {
            case Quest q -> addQuest(q);
            case QuestLink ql -> addQuestLink(ql);
            case ChapterImage img -> addImage(img);
            default -> throw new IllegalArgumentException("expecting quest, quest link or chapter image!");
        }
	}

	public void removeChildObject(Movable child) {
		switch (child) {
			case Quest q -> removeQuest(q);
			case QuestLink ql -> removeQuestLink(ql);
			case ChapterImage img -> removeImage(img);
			default -> throw new IllegalArgumentException("expecting quest, quest link or chapter image!");
		}
	}

	@Override
	public void writeData(Json5Object json, HolderLookup.Provider provider) {
		super.writeData(json, provider);

		json.addProperty("filename", filename);
		if (alwaysInvisible) json.addProperty("always_invisible", true);
		json.addProperty("default_quest_shape", defaultQuestShape);
		if (defaultQuestSize != 1D) json.addProperty("default_quest_size", defaultQuestSize);
		json.addProperty("default_hide_dependency_lines", defaultHideDependencyLines);
		if (defaultMinWidth > 0) json.addProperty("default_min_width", defaultMinWidth);
		if (progressionMode != ProgressionMode.DEFAULT) json.addProperty("progression_mode", progressionMode.getId());
		consumeItems.write(json, "consume_items");
		if (hideQuestDetailsUntilStartable) json.addProperty("hide_quest_details_until_startable", true);
		if (hideQuestUntilDepsVisible) json.addProperty("hide_quest_until_deps_visible", true);
		if (hideQuestUntilDepsComplete) json.addProperty("hide_quest_until_deps_complete", true);
		if (hideTextUntilComplete) json.addProperty("hide_text_until_complete", true);
		if (defaultRepeatable) json.addProperty("default_repeatable_quest", true);
		if (requireSequentialTasks) json.addProperty("require_sequential_tasks", true);

		if (!autoFocusId.isEmpty()) json.addProperty("autofocus_id", autoFocusId);
	}

	@Override
	public void readData(Json5Object json, HolderLookup.Provider provider) {
		super.readData(json, provider);

		filename = Json5Util.getString(json, "filename").orElseThrow();
		alwaysInvisible = Json5Util.getBoolean(json, "always_invisible").orElse(false);
		defaultQuestShape = Json5Util.getString(json, "default_quest_shape").orElseThrow();
		defaultQuestSize = Json5Util.getDouble(json, "default_quest_size").orElse(1D);
		defaultHideDependencyLines = Json5Util.getBoolean(json, "default_hide_dependency_lines").orElse(false);
		defaultMinWidth = Json5Util.getInt(json, "default_min_width").orElse(0);
		progressionMode = Json5Util.getString(json, "progression_mode").map(ProgressionMode.NAME_MAP::get).orElse(ProgressionMode.DEFAULT);
		consumeItems = Tristate.read(json, "consume_items");
		hideQuestDetailsUntilStartable = Json5Util.getBoolean(json, "hide_quest_details_until_startable").orElse(false);
		hideQuestUntilDepsVisible = Json5Util.getBoolean(json, "hide_quest_until_deps_visible").orElse(false);
		hideQuestUntilDepsComplete = Json5Util.getBoolean(json, "hide_quest_until_deps_complete").orElse(false);
		hideTextUntilComplete = Json5Util.getBoolean(json, "hide_text_until_complete").orElse(false);
		defaultRepeatable = Json5Util.getBoolean(json, "default_repeatable_quest").orElse(false);
		requireSequentialTasks = Json5Util.getBoolean(json, "require_sequential_tasks").orElse(false);
		autoFocusId = Json5Util.getString(json, "autofocus_id").orElse("");

		if (defaultQuestShape.equals("default")) {
			defaultQuestShape = "";
		}
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(filename, Short.MAX_VALUE);
		buffer.writeUtf(defaultQuestShape, Short.MAX_VALUE);
		buffer.writeDouble(defaultQuestSize);
		buffer.writeInt(defaultMinWidth);
		ProgressionMode.NAME_MAP.write(buffer, progressionMode);

		buffer.writeVarInt(makeFlags());

		if (!autoFocusId.isEmpty()) buffer.writeLong(QuestObjectBase.parseHexId(autoFocusId).orElse(0L));
	}

	private int makeFlags() {
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
		flags = Bits.setFlag(flags, 0x400, hideTextUntilComplete);
		return flags;
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		filename = buffer.readUtf(Short.MAX_VALUE);
		defaultQuestShape = buffer.readUtf(Short.MAX_VALUE);
		defaultQuestSize = buffer.readDouble();
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
		hideTextUntilComplete = Bits.getFlag(flags, 0x400);

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
			if (!quest.isOptionalForProgression(data)) {
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
	public void onStarted(ProgressEventData<?> data) {
		data.setStarted(id);
		NativeEventPosting.get().postEvent(new ChapterProgressEvent.Data(ProgressType.STARTED, data.withObject(this)));

		if (!data.teamData().isStarted(file)) {
			file.onStarted(data.withObject(file));
		}
	}

	@Override
	public void onCompleted(ProgressEventData<?> data) {
		data.setCompleted(id);
		NativeEventPosting.get().postEvent(new ChapterProgressEvent.Data(ProgressType.COMPLETED, data.withObject(this)));

		if (!disableToast) {
			data.notifyPlayers(id);
		}

		file.forAllQuests(quest -> {
			if (quest.hasDependency(this)) {
				data.teamData().checkAutoCompletion(quest);
			}
		});

		if (group.isCompletedRaw(data.teamData())) {
			group.onCompleted(data.withObject(group));
		}
	}

	@Override
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.unnamed");
	}

	@Override
	public Icon<?> getAltIcon() {
		List<Icon<?>> list = new ArrayList<>();

		for (Quest quest : quests) {
			list.add(quest.getIcon());
		}

		return AnimatedIcon.fromList(list, false);
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
		// filename should have been suggested by the client and available here
		// but in case not, fall back to the chapter's hex object id
		if (filename.isEmpty()) {
			filename = getCodeString();
		}

		// ensure the filename is actually unique (same chapter name could appear in multiple groups...)
		Set<String> existingNames = new HashSet<>();
		getQuestFile().forAllChapters(ch -> existingNames.add(ch.filename));
		if (existingNames.contains(filename)) {
			filename = filename + "_" + getCodeString();
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
	public void fillConfigGroup(EditableConfigGroup config) {
		super.fillConfigGroup(config);

		config.addList("subtitle", getRawSubtitle(), new EditableString(), this::setRawSubtitle, "");

		EditableConfigGroup appearance = config.getOrCreateSubgroup("appearance").setNameKey("ftbquests.quest.appearance");
		appearance.addEnum("default_quest_shape", defaultQuestShape.isEmpty() ? "default" : defaultQuestShape, v -> defaultQuestShape = v.equals("default") ? "" : v, QuestShape.idMapWithDefault);
		appearance.addDouble("default_quest_size", defaultQuestSize, v -> defaultQuestSize = v, 1, 0.0625D, 8D);
		appearance.addInt("default_min_width", defaultMinWidth, v -> defaultMinWidth = v, 0, 0, 3000);

		EditableConfigGroup visibility = config.getOrCreateSubgroup("visibility").setNameKey("ftbquests.quest.visibility");
		visibility.addBool("always_invisible", alwaysInvisible, v -> alwaysInvisible = v, false);
		visibility.addBool("default_hide_dependency_lines", defaultHideDependencyLines, v -> defaultHideDependencyLines = v, false);
		visibility.addBool("hide_quest_details_until_startable", hideQuestDetailsUntilStartable, v -> hideQuestDetailsUntilStartable = v, false);
		visibility.addBool("hide_quest_until_deps_visible", hideQuestUntilDepsVisible, v -> hideQuestUntilDepsVisible = v, false);
		visibility.addBool("hide_quest_until_deps_complete", hideQuestUntilDepsComplete, v -> hideQuestUntilDepsComplete = v, false);
		visibility.addBool("hide_text_until_complete", hideTextUntilComplete, v -> hideTextUntilComplete = v, false);

		EditableConfigGroup misc = config.getOrCreateSubgroup("misc").setNameKey("ftbquests.quest.misc");
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

	public List<String> getRawSubtitle() {
		return file.getTranslationManager().getStringListTranslation(this, file.getLocale(), TranslationKey.CHAPTER_SUBTITLE)
				.orElse(List.of());
	}

	public void setRawSubtitle(List<String> rawSubtitle) {
		setTranslatableValue(TranslationKey.CHAPTER_SUBTITLE, rawSubtitle);
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
		if (!autoFocusId.isEmpty()) {
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
