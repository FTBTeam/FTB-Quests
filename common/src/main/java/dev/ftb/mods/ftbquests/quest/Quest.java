package dev.ftb.mods.ftbquests.quest;

import com.mojang.datafixers.util.Pair;
import dev.ftb.mods.ftblibrary.config.*;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.Vec2d;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.gui.MultilineTextEditorScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.ftb.mods.ftbquests.events.ObjectStartedEvent;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.quest.preset.VisualPreset;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import dev.ftb.mods.ftbquests.util.FTBQCodecs;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import dev.ftb.mods.ftbquests.util.TextUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class Quest extends QuestObject implements Movable, Excludable {
	public static final String PAGEBREAK_CODE = "{@pagebreak}";

	public static final StreamCodec<FriendlyByteBuf, Map<Long, Pair<Vec2d, Vec2d>>> CONTROL_POINTS_STREAM_CODEC
			= ByteBufCodecs.map(HashMap::new, ByteBufCodecs.VAR_LONG, FTBQCodecs.pair(Vec2d.STREAM_CODEC, Vec2d.STREAM_CODEC));

	private Chapter chapter;
	private double x, y;
	private Tristate hideUntilDepsVisible;
	private Tristate hideUntilDepsComplete;
	private String shape;
	private final List<QuestObject> dependencies;
	private final Map<Long, Pair<Vec2d, Vec2d>> depControlPoints;  // for bezier curved depenendency lines
	private final List<Task> tasks;
	private final List<Reward> rewards;
	private DependencyRequirement dependencyRequirement;
	private String guidePage;
	private Tristate hideDependencyLines;
	private boolean hideDependentLines;
	private int minRequiredDependencies;
	private Tristate hideTextUntilComplete;
	private Tristate disableJEI;
	private Tristate hideDetailsUntilStartable;
	private double size;
	private boolean optional;
	private int minWidth;
	private Tristate canRepeat;
	private boolean invisibleUntilCompleted;
	private int invisibleUntilTasks;  // invisible until at least X number of tasks have been completed
	private Tristate requireSequentialTasks;
	private double iconScale;
	private boolean hideLockIcon;
	private int maxCompletableDeps;
	private int repeatCooldown; // seconds
	private String presetName;
	private VisualPreset cachedPreset;

	private Component cachedSubtitle = null;
	private List<Component> cachedDescription = null;
	private boolean ignoreRewardBlocking;
	private ProgressionMode progressionMode;
	private final Set<Long> dependantIDs;

	public Quest(long id, Chapter chapter) {
		super(id);

		this.chapter = chapter;

		x = 0;
		y = 0;
		shape = "";
		dependencies = new ArrayList<>(0);
		depControlPoints = new Long2ObjectOpenHashMap<>();
		tasks = new ArrayList<>(1);
		rewards = new ArrayList<>(1);
		guidePage = "";
		hideDependencyLines = Tristate.DEFAULT;
		hideDependentLines = false;
		hideUntilDepsVisible = Tristate.DEFAULT;
		hideUntilDepsComplete = Tristate.DEFAULT;
		dependencyRequirement = DependencyRequirement.ALL_COMPLETED;
		minRequiredDependencies = 0;
		hideTextUntilComplete = Tristate.DEFAULT;
		disableJEI = Tristate.DEFAULT;
		hideDetailsUntilStartable = Tristate.DEFAULT;
		size = 0D;
		optional = false;
		minWidth = 0;
		canRepeat = Tristate.DEFAULT;
		invisibleUntilCompleted = false;
		invisibleUntilTasks = 0;
		ignoreRewardBlocking = false;
		progressionMode = ProgressionMode.DEFAULT;
		dependantIDs = new HashSet<>();
		requireSequentialTasks = Tristate.DEFAULT;
		iconScale = 1d;
		hideLockIcon = false;
		maxCompletableDeps = 0;
		repeatCooldown = 0;
		presetName = "";
	}

	@Override
	public QuestObjectType getObjectType() {
		return QuestObjectType.QUEST;
	}

	@Override
	public BaseQuestFile getQuestFile() {
		return chapter.file;
	}

	@Override
	public Chapter getQuestChapter() {
		return chapter;
	}

	@Override
	public long getParentID() {
		return chapter.id;
	}

	public Collection<Task> getTasks() {
		return Collections.unmodifiableList(tasks);
	}

	public List<Task> getTasksAsList() {
		return Collections.unmodifiableList(tasks);
	}

	public Collection<Reward> getRewards() {
		return Collections.unmodifiableList(rewards);
	}

	public int getMinRequiredDependencies() {
		return minRequiredDependencies;
	}

	public boolean shouldHideDependentLines() {
		return hideDependentLines;
	}

	public String getGuidePage() {
		return guidePage;
	}

	public Tristate getHideTextUntilComplete() {
		return hideTextUntilComplete;
	}

	// used by FTB XMod Compat
	@SuppressWarnings("unused")
	public boolean showInRecipeMod() {
		return !disableJEI.get(getQuestFile().isDefaultQuestDisableJEI());
	}

	public String getRawSubtitle() {
		return getQuestFile().getTranslationManager().getStringTranslation(this, getQuestFile().getLocale(), TranslationKey.QUEST_SUBTITLE)
				.orElse("");
	}

	public void setRawSubtitle(String rawSubtitle) {
		setTranslatableValue(TranslationKey.QUEST_SUBTITLE, rawSubtitle);
		cachedSubtitle = null;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getSize() {
		double size = getVisualPreset().size();
		return size == 0D ? chapter.getDefaultQuestSize() : size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public boolean isOptional() {
		return optional;
	}

	public int getMinWidth() {
		return minWidth;
	}

	public boolean canBeRepeated() {
		return canRepeat.get(chapter.isDefaultRepeatable());
	}

	private VisualPreset getVisualPreset() {
		if (cachedPreset == null) {
			String name = presetName.isEmpty() ? chapter.getPresetName() : presetName;
			if (name.isEmpty()) {
				cachedPreset = new FallbackVisualPreset();
			} else {
				cachedPreset = getQuestFile().getPresets().get(name).orElseGet(FallbackVisualPreset::new);
			}
		}
		return cachedPreset;
	}

	public List<String> getRawDescription() {
		return getQuestFile().getTranslationManager().getStringListTranslation(this, getQuestFile().getLocale(), TranslationKey.QUEST_DESC)
				.orElse(List.of());
	}

	public void setRawDescription(List<String> rawDescription) {
		setTranslatableValue(TranslationKey.QUEST_DESC, rawDescription);
		cachedDescription = null;
	}

	public double getIconScale() {
		return iconScale;
	}

	@Override
	public boolean isOptionalForProgression(TeamData teamData) {
		return isOptional() || canBeRepeated() || teamData.isExcludedByOtherQuestline(this);
	}

	public boolean getRequireSequentialTasks() {
		return requireSequentialTasks.get(chapter.isRequireSequentialTasks());
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);

		nbt.putDouble("x", x);
		nbt.putDouble("y", y);

		if (!shape.isEmpty()) {
			nbt.putString("shape", shape);
		}

		if (!guidePage.isEmpty()) {
			nbt.putString("guide_page", guidePage);
		}

		hideDependencyLines.write(nbt, "hide_dependency_lines");

		if (hideDependentLines) {
			nbt.putBoolean("hide_dependent_lines", true);
		}

		if (minRequiredDependencies > 0) {
			nbt.putInt("min_required_dependencies", (byte) minRequiredDependencies);
		}

		removeInvalidDependencies();

		if (hasDependencies()) {
			ListTag deps = new ListTag();
			for (QuestObject dep : dependencies) {
				deps.add(StringTag.valueOf(dep.getCodeString()));
			}
			nbt.put("dependencies", deps);
			if (!depControlPoints.isEmpty()) {
				nbt.put("dep_control_pts", writeDepControlPoints());
			}
		}

		disableJEI.write(nbt, "disable_recipe_mod");
		hideUntilDepsVisible.write(nbt, "hide_until_deps_visible");
		hideUntilDepsComplete.write(nbt, "hide_until_deps_complete");

		if (dependencyRequirement != DependencyRequirement.ALL_COMPLETED) {
			nbt.putString("dependency_requirement", dependencyRequirement.getId());
		}

		hideTextUntilComplete.write(nbt,"hide_text_until_complete");

		if (size != 0D) {
			nbt.putDouble("size", size);
		}

		if (iconScale != 1d) {
			nbt.putDouble("icon_scale", iconScale);
		}

		if (optional) {
			nbt.putBoolean("optional", true);
		}

		if (minWidth > 0) {
			nbt.putInt("min_width", minWidth);
		}

		canRepeat.write(nbt, "can_repeat");

		if (invisibleUntilCompleted) {
			nbt.putBoolean("invisible", true);
		}
		if (invisibleUntilTasks > 0) {
			nbt.putInt("invisible_until_tasks", invisibleUntilTasks);
		}

		if (ignoreRewardBlocking) {
			nbt.putBoolean("ignore_reward_blocking", true);
		}

		if (progressionMode != ProgressionMode.DEFAULT) {
			nbt.putString("progression_mode", progressionMode.getId());
		}

		hideDetailsUntilStartable.write(nbt, "hide_details_until_startable");
		requireSequentialTasks.write(nbt, "require_sequential_tasks");

		if (hideLockIcon) {
			nbt.putBoolean("hide_lock_icon", true);
		}

		if (maxCompletableDeps > 0) {
			nbt.putInt("max_completable_dependents", maxCompletableDeps);
		}

		if (repeatCooldown > 0) {
			nbt.putInt("repeat_cooldown", repeatCooldown);
		}

		if (!presetName.isEmpty()) nbt.putString("preset", presetName);
	}

	private Tag writeDepControlPoints() {
		CompoundTag tag = new CompoundTag();
		depControlPoints.forEach((id, points) ->
				tag.put(QuestObjectBase.getCodeString(id), Util.make(new ListTag(), t -> {
					t.add(DoubleTag.valueOf(points.getFirst().x()));
					t.add(DoubleTag.valueOf(points.getFirst().y()));
					t.add(DoubleTag.valueOf(points.getSecond().x()));
					t.add(DoubleTag.valueOf(points.getSecond().y()));
				}))
		);
		return tag;
	}

	private Map<Long, Pair<Vec2d, Vec2d>> readDepControlPoints(CompoundTag tag) {
		Map<Long, Pair<Vec2d, Vec2d>> map = new HashMap<>();
		for (String k : tag.getAllKeys()) {
			try {
				ListTag l = tag.getList(k, Tag.TAG_DOUBLE);
				if (l.size() == 4) {
					map.put(QuestObjectBase.parseCodeString(k), Pair.of(
							new Vec2d(l.getDouble(0), l.getDouble(1)),
							new Vec2d(l.getDouble(2), l.getDouble(3)))
					);
				}
			} catch (NumberFormatException ignored) {
			}
		}
		return map;
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);

		x = nbt.getDouble("x");
		y = nbt.getDouble("y");
		shape = nbt.getString("shape");

		if (shape.equals("default")) {
			shape = "";
		}

		guidePage = nbt.getString("guide_page");
		hideDependencyLines = Tristate.read(nbt, "hide_dependency_lines");
		hideDependentLines = nbt.getBoolean("hide_dependent_lines");
		minRequiredDependencies = nbt.getInt("min_required_dependencies");

		clearDependencies();

		if (nbt.contains("dependencies", 11)) {
			for (int i : nbt.getIntArray("dependencies")) {
				QuestObject object = chapter.file.get(i);

				if (object != null) {
					addDependency(object);
				}
			}
		} else {
			ListTag deps = nbt.getList("dependencies", Tag.TAG_STRING);

			for (int i = 0; i < deps.size(); i++) {
				QuestObject object = chapter.file.get(chapter.file.getID(deps.getString(i)));

				if (object != null) {
					addDependency(object);
				}
			}
		}
		if (nbt.contains("dep_control_pts", Tag.TAG_COMPOUND)) {
			depControlPoints.putAll(readDepControlPoints(nbt.getCompound("dep_control_pts")));
		}

		if (nbt.contains("hide", Tag.TAG_BYTE)) {
			// TODO legacy; remove in 1.22
			hideUntilDepsVisible = Tristate.read(nbt, "hide");
		} else {
			hideUntilDepsVisible = Tristate.read(nbt, "hide_until_deps_visible");
		}
		hideUntilDepsComplete = Tristate.read(nbt, "hide_until_deps_complete");
		disableJEI = Tristate.read(nbt, "disable_recipe_mod");
		dependencyRequirement = DependencyRequirement.NAME_MAP.get(nbt.getString("dependency_requirement"));
		hideTextUntilComplete = Tristate.read(nbt, "hide_text_until_complete");
		size = nbt.getDouble("size");
		iconScale = nbt.contains("icon_scale", Tag.TAG_DOUBLE) ? nbt.getDouble("icon_scale") : 1f;
		optional = nbt.getBoolean("optional");
		minWidth = nbt.getInt("min_width");
		canRepeat = Tristate.read(nbt, "can_repeat");
		invisibleUntilCompleted = nbt.getBoolean("invisible");
		invisibleUntilTasks = nbt.getInt("invisible_until_tasks");
		ignoreRewardBlocking = nbt.getBoolean("ignore_reward_blocking");
		progressionMode = ProgressionMode.NAME_MAP.get(nbt.getString("progression_mode"));
		hideDetailsUntilStartable = Tristate.read(nbt, "hide_details_until_startable");
		requireSequentialTasks = Tristate.read(nbt, "require_sequential_tasks");
		hideLockIcon = nbt.getBoolean("hide_lock_icon");
		maxCompletableDeps = nbt.getInt("max_completable_dependents");
		repeatCooldown = nbt.getInt("repeat_cooldown");
		presetName = nbt.getString("preset");
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		int flags = 0;
		flags = Bits.setFlag(flags, 0x04, size != 0D);
		flags = Bits.setFlag(flags, 0x08, !guidePage.isEmpty());
		flags = Bits.setFlag(flags, 0x10, ignoreRewardBlocking);
		flags = Bits.setFlag(flags, 0x20, hideDependentLines);
		//implement others
		//flags = Bits.setFlag(flags, 32, !customClick.isEmpty());
		flags = Bits.setFlag(flags, 0x80, invisibleUntilCompleted);
		flags = Bits.setFlag(flags, 0x100, optional);
		flags = Bits.setFlag(flags, 0x200, minWidth > 0);
		flags = Bits.setFlag(flags, 0x400, invisibleUntilTasks > 0);
		flags = Bits.setFlag(flags, 0x800, hideDetailsUntilStartable != Tristate.DEFAULT);
		flags = Bits.setFlag(flags, 0x1000, hideDetailsUntilStartable == Tristate.TRUE);
		flags = Bits.setFlag(flags, 0x2000, canRepeat != Tristate.DEFAULT);
		flags = Bits.setFlag(flags, 0x4000, canRepeat == Tristate.TRUE);
		flags = Bits.setFlag(flags, 0x8000, requireSequentialTasks != Tristate.DEFAULT);
		flags = Bits.setFlag(flags, 0x10000, requireSequentialTasks == Tristate.TRUE);
		flags = Bits.setFlag(flags, 0x20000, iconScale != 1f);
		flags = Bits.setFlag(flags, 0x40000, hideLockIcon);
		flags = Bits.setFlag(flags, 0x80000, disableJEI != Tristate.DEFAULT);
		flags = Bits.setFlag(flags, 0x100000, disableJEI == Tristate.TRUE);
		buffer.writeVarInt(flags);

		hideUntilDepsComplete.write(buffer);
		hideUntilDepsVisible.write(buffer);
		hideDependencyLines.write(buffer);
		hideTextUntilComplete.write(buffer);

		buffer.writeDouble(x);
		buffer.writeDouble(y);
		buffer.writeUtf(shape, Short.MAX_VALUE);

		if (!guidePage.isEmpty()) {
			buffer.writeUtf(guidePage, Short.MAX_VALUE);
		}

		buffer.writeVarInt(minRequiredDependencies);
		DependencyRequirement.NAME_MAP.write(buffer, dependencyRequirement);

		FTBQCodecs.LONG_LIST_STREAM_CODEC.encode(buffer, dependencies.stream().map(QuestObject::getId).toList());
		CONTROL_POINTS_STREAM_CODEC.encode(buffer, depControlPoints);

		if (size != 0D) {
			buffer.writeDouble(size);
		}

		if (iconScale != 1D) {
			buffer.writeDouble(iconScale);
		}

		if (minWidth > 0) {
			buffer.writeVarInt(minWidth);
		}

		if (invisibleUntilTasks > 0) {
			buffer.writeVarInt(invisibleUntilTasks);
		}

		ProgressionMode.NAME_MAP.write(buffer, progressionMode);

		buffer.writeVarInt(maxCompletableDeps);
		if (canBeRepeated()) {
			buffer.writeInt(repeatCooldown);
		}

		buffer.writeUtf(presetName);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		int flags = buffer.readVarInt();
		hideUntilDepsComplete = Tristate.read(buffer);
		hideUntilDepsVisible = Tristate.read(buffer);
		hideDependencyLines = Tristate.read(buffer);
		hideTextUntilComplete = Tristate.read(buffer);

		x = buffer.readDouble();
		y = buffer.readDouble();
		shape = buffer.readUtf(Short.MAX_VALUE);

		guidePage = Bits.getFlag(flags, 0x08) ? buffer.readUtf(Short.MAX_VALUE) : "";

		minRequiredDependencies = buffer.readVarInt();
		dependencyRequirement = DependencyRequirement.NAME_MAP.read(buffer);

		clearDependencies();
		FTBQCodecs.LONG_LIST_STREAM_CODEC.decode(buffer).forEach(this::addDependency);
		depControlPoints.putAll(CONTROL_POINTS_STREAM_CODEC.decode(buffer));

		size = Bits.getFlag(flags, 0x04) ? buffer.readDouble() : 0D;
		iconScale = Bits.getFlag(flags, 0x20000) ? buffer.readDouble() : 1D;
		minWidth = Bits.getFlag(flags, 0x200) ? buffer.readVarInt() : 0;
		ignoreRewardBlocking = Bits.getFlag(flags, 0x10);
		hideDependentLines = Bits.getFlag(flags, 0x20);
		canRepeat = Bits.getFlag(flags, 0x2000) ? Bits.getFlag(flags, 0x4000) ? Tristate.TRUE : Tristate.FALSE : Tristate.DEFAULT;
		invisibleUntilCompleted = Bits.getFlag(flags, 0x80);
		optional = Bits.getFlag(flags, 0x100);
		invisibleUntilTasks = Bits.getFlag(flags, 0x400) ? buffer.readVarInt() : 0;
		hideDetailsUntilStartable = Bits.getFlag(flags, 0x800) ? Bits.getFlag(flags, 0x1000) ? Tristate.TRUE : Tristate.FALSE : Tristate.DEFAULT;
		requireSequentialTasks = Bits.getFlag(flags, 0x8000) ? Bits.getFlag(flags, 0x10000) ? Tristate.TRUE : Tristate.FALSE : Tristate.DEFAULT;
		disableJEI = Bits.getFlag(flags, 0x80000) ? Bits.getFlag(flags, 0x100000) ? Tristate.TRUE : Tristate.FALSE : Tristate.DEFAULT;
		hideLockIcon = Bits.getFlag(flags, 0x40000);
		progressionMode = ProgressionMode.NAME_MAP.read(buffer);
		maxCompletableDeps = buffer.readVarInt();
		repeatCooldown = canBeRepeated() ? buffer.readInt() : 0;
		presetName = buffer.readUtf();
	}

	@Override
	public int getRelativeProgressFromChildren(TeamData data) {
		if (data.getCompletionCount(this) > 0) {
			return 100;
		}

		if (tasks.isEmpty()) {
			return data.areDependenciesComplete(this) ? 100 : 0;
		}

		int progress = 0;

		for (Task task : tasks) {
			progress += data.getRelativeProgress(task);
		}

		if (progress > 0 && !data.areDependenciesComplete(this)) {
			return 0;
		}

		return getRelativeProgressFromChildren(progress, tasks.size());
	}

	@Override
	public void onStarted(QuestProgressEventData<?> data) {
		data.setStarted(id);
		ObjectStartedEvent.QUEST.invoker().act(new ObjectStartedEvent.QuestEvent(data.withObject(this)));

		if (!data.getTeamData().isStarted(chapter)) {
			chapter.onStarted(data.withObject(chapter));
		}
	}

	@Override
	public void onCompleted(QuestProgressEventData<?> data) {
		if (!data.getTeamData().isCompleted(this)) {
			// it's possible this quest is already completed, if it has one or more optional tasks
			data.setCompleted(id);
			ObjectCompletedEvent.QUEST.invoker().act(new ObjectCompletedEvent.QuestEvent(data.withObject(this)));

			if (!disableToast) {
				data.notifyPlayers(id);
			}

			if (!data.getTeamData().isCompleted(chapter) && chapter.isCompletedRaw(data.getTeamData())) {
				chapter.onCompleted(data.withObject(chapter));
			}

			data.getTeamData().checkAutoCompletion(this);

			checkForDependantCompletion(data.getTeamData());
		}
	}

	private void checkForDependantCompletion(TeamData data) {
		getDependants().forEach(questObject -> {
			if (questObject instanceof Quest quest) {
				if (quest.getProgressionMode() == ProgressionMode.FLEXIBLE && quest.checkDependencies(data::isCompleted)) {
					quest.tasks.forEach(task -> {
						if (data.getProgress(task.id) >= task.getMaxProgress()) {
							data.markTaskCompleted(task);
						}
					});
				}

				data.checkAutoCompletion(quest);
			}
		});
	}

	public ProgressionMode getProgressionMode() {
		return progressionMode == ProgressionMode.DEFAULT ? chapter.getProgressionMode() : progressionMode;
	}

	@Override
	public void forceProgress(TeamData teamData, ProgressChange progressChange) {
		super.forceProgress(teamData, progressChange);

		for (Reward r : rewards) {
			r.forceProgress(teamData, progressChange);
		}
	}

	@Override
	public Component getAltTitle() {
		if (!tasks.isEmpty()) {
			return tasks.getFirst().getTitle();
		}

		return Component.translatable("ftbquests.unnamed");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		List<Icon> list = new ArrayList<>();

		for (Task task : tasks) {
			list.add(task.getIcon());
		}

		return IconAnimation.fromList(list, false);
	}

	@Override
	public void deleteSelf() {
		super.deleteSelf();

		chapter.removeQuest(this);

		// clean up any tasks & rewards
		for (Task task : List.copyOf(tasks)) {
			task.deleteSelf();
		}
		tasks.clear();
		for (Reward reward : List.copyOf(rewards)) {
			reward.deleteSelf();
		}
		rewards.clear();

		// clean up any quest links which point to this quest
		List<QuestLink> linksToDel = new ArrayList<>();
		getQuestFile().forAllQuestLinks(link -> {
			if (link.linksTo(this)) {
				linksToDel.add(link);
			}
		});
		linksToDel.forEach(QuestLink::deleteSelf);
	}

	@Override
	public void onCreated() {
		super.onCreated();

		chapter.addQuest(this);

		if (!tasks.isEmpty()) {
			List<Task> l = new ArrayList<>(tasks);
			tasks.clear();
			for (Task task : l) {
				task.onCreated();
			}
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);

		config.addString("subtitle", getRawSubtitle(), this::setRawSubtitle, "");
		StringConfig descType = new StringConfig();
		config.add("description", new ListConfig<String, StringConfig>(descType) {
			@Override
			public void onClicked(Widget clicked, MouseButton button, ConfigCallback callback) {
				new MultilineTextEditorScreen(Component.translatable("ftbquests.gui.edit_description"), this, callback).openGui();
			}
		}, getRawDescription(), this::setRawDescription, Collections.emptyList());

		ConfigGroup appearance = config.getOrCreateSubgroup("appearance");
		appearance.addEnum("shape", shape.isEmpty() ? "default" : shape, v -> shape = v.equals("default") ? "" : v, QuestShape.idMapWithDefault);
		appearance.addDouble("size", size, v -> size = v, 0, 0, 8D);
		appearance.addDouble("x", x, v -> x = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		appearance.addDouble("y", y, v -> y = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		appearance.addInt("min_width", minWidth, v -> minWidth = v, 0, 0, 3000);
		appearance.addDouble("icon_scale", iconScale, v -> iconScale = v, 1f, 0.1, 2.0);
		appearance.addEnum("preset", presetName, v -> presetName = v, getQuestFile().getPresets().nameMap());

		ConfigGroup visibility = config.getOrCreateSubgroup("visibility");
		visibility.addTristate("hide_until_deps_complete", hideUntilDepsComplete, v -> hideUntilDepsComplete = v);
		visibility.addTristate("hide_until_deps_visible", hideUntilDepsVisible, v -> hideUntilDepsVisible = v);
		var editableIUC = visibility.addBool("invisible", invisibleUntilCompleted, v -> invisibleUntilCompleted = v, false);
		visibility.addInt("invisible_until_tasks", invisibleUntilTasks, v -> invisibleUntilTasks = v, 0, 0, Integer.MAX_VALUE).setCanEdit(editableIUC::getValue);
		visibility.addTristate("hide_details_until_startable", hideDetailsUntilStartable, v -> hideDetailsUntilStartable = v);
		visibility.addTristate("hide_text_until_complete", hideTextUntilComplete, v -> hideTextUntilComplete = v);
		visibility.addBool("hide_lock_icon", hideLockIcon, v -> hideLockIcon = v, false);

		Predicate<QuestObjectBase> depTypes = object -> object != chapter.file && object != chapter && object instanceof QuestObject;
		removeInvalidDependencies();
		ConfigGroup deps = config.getOrCreateSubgroup("dependencies");
		deps.addList("dependencies", dependencies, new ConfigQuestObject<>(depTypes), null).setNameKey("ftbquests.dependencies");
		deps.addEnum("dependency_requirement", dependencyRequirement, v -> dependencyRequirement = v, DependencyRequirement.NAME_MAP);
		deps.addInt("min_required_dependencies", minRequiredDependencies, v -> minRequiredDependencies = v, 0, 0, Integer.MAX_VALUE);
		deps.addTristate("hide_dependency_lines", hideDependencyLines, v -> hideDependencyLines = v);
		deps.addBool("hide_dependent_lines", hideDependentLines, v -> hideDependentLines = v, false);
		deps.addInt("max_completable_dependents", maxCompletableDeps, v -> maxCompletableDeps = v, 0, 0, Integer.MAX_VALUE);

		ConfigGroup misc = config.getOrCreateSubgroup("misc");
		misc.addString("guide_page", guidePage, v -> guidePage = v, "");
		misc.addEnum("disable_jei", disableJEI, v -> disableJEI = v, Tristate.NAME_MAP);
		misc.addTristate("can_repeat", canRepeat, v -> canRepeat = v);
		misc.addInt("repeat_cooldown", repeatCooldown, v -> repeatCooldown = v, 0, 0, Integer.MAX_VALUE);
		misc.addBool("optional", optional, v -> optional = v, false);
		misc.addBool("ignore_reward_blocking", ignoreRewardBlocking, v -> ignoreRewardBlocking = v, false);
		misc.addEnum("progression_mode", progressionMode, v -> progressionMode = v, ProgressionMode.NAME_MAP);
		misc.addTristate("require_sequential_tasks", requireSequentialTasks, v -> requireSequentialTasks = v);
	}

	public boolean shouldHideDependencyLines() {
		return hideDependencyLines.get(chapter.defaultHideDependencyLines);
	}

	public boolean shouldHideLockIcon() {
		return hideLockIcon;
	}

	@Override
	public long getMovableID() {
		return id;
	}

	@Override
	public Chapter getChapter() {
		return chapter;
	}

	@Override
	public void setChapter(Chapter newChapter) {
		if (!Objects.equals(getChapter(), newChapter)) {
			getChapter().removeQuest(this);
			newChapter.addQuest(this);
			this.chapter = newChapter;
		}
	}

	@Override
	public Quest setPosition(double x, double y) {
		this.x = x;
		this.y = y;
		getQuestFile().markDirty();
		return this;
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public double getWidth() {
		return getSize();
	}

	@Override
	public double getHeight() {
		return getSize();
	}

	@Override
	public String getShape() {
		String shape = getVisualPreset().shapeName();
		return shape.isEmpty() ? chapter.getDefaultQuestShape() : shape;
	}

	@Override
	public boolean isVisible(TeamData data) {
		if (invisibleUntilCompleted && !data.isCompleted(this)) {
			if (invisibleUntilTasks == 0 || tasks.stream().filter(data::isCompleted).limit(invisibleUntilTasks).count() < invisibleUntilTasks) {
				return false;
			}
		}

		if (data.getFile().isHideExcludedQuests() && data.isExcludedByOtherQuestline(this)) {
			return false;
		}

		if (dependencies.isEmpty()) {
			return true;
		}

		if (hideUntilDepsComplete.get(chapter.hideQuestUntilDepsComplete())) {
			return data.areDependenciesComplete(this);
		} else if (hideUntilDepsVisible.get(chapter.isHideQuestUntilDepsVisible())) {
			return data.areDependenciesVisible(this);
		}

		return streamDependencies().anyMatch(object -> object.isVisible(data));
	}

	@Override
	public boolean isSearchable(TeamData data) {
		return !chapter.isAlwaysInvisible() && super.isSearchable(data);
	}

	@Override
	public void clearCachedData() {
		super.clearCachedData();
		cachedSubtitle = null;
		cachedDescription = null;
		cachedPreset = null;

		for (Task task : tasks) {
			task.clearCachedData();
		}

		for (Reward reward : rewards) {
			reward.clearCachedData();
		}
	}

	@Environment(EnvType.CLIENT)
	public Component getSubtitle() {
		if (cachedSubtitle == null) {
			cachedSubtitle = TextUtils.parseRawText(getRawSubtitle(), holderLookup());
		}
		return cachedSubtitle;
	}

	@Environment(EnvType.CLIENT)
	public List<Component> getDescription() {
		if (cachedDescription == null) {
			cachedDescription = getRawDescription().stream().map(str -> TextUtils.parseRawText(str, holderLookup())).toList();
		}
		return cachedDescription;
	}

	public boolean hasDependency(QuestObject object) {
		if (object.isValid()) {
			for (QuestObject dependency : dependencies) {
				if (dependency == object) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected boolean validateEditedConfig() {
		try {
			verifyDependenciesInternal(id, 0);
			return true;
		} catch (DependencyDepthException | DependencyLoopException ex) {
			clearDependencies();
			// should always be on the client at this point, but let's be paranoid
			if (!getQuestFile().isServerSide()) {
				QuestScreen.displayError(Component.translatable("ftbquests.gui.looping_dependencies"));
			}
			return false;
		}
	}

	public boolean verifyDependencies(boolean autofix) {
		try {
			verifyDependenciesInternal(id, 0);
			return true;
		} catch (DependencyDepthException ex) {
			if (autofix) {
				FTBQuests.LOGGER.error("Too deep dependencies found in {} (referenced in {})! Deleting all dependencies...", this, ex.object);
				clearDependencies();
				chapter.file.markDirty();
			} else {
				FTBQuests.LOGGER.error("Too deep dependencies found in {} (referenced in {})!", this, ex.object);
			}

			return false;
		} catch (DependencyLoopException ex) {
			if (autofix) {
				FTBQuests.LOGGER.error("Looping dependencies found in {} (referenced in {})! Deleting all dependencies...", this, ex.object);
				clearDependencies();
				chapter.file.markDirty();
			} else {
				FTBQuests.LOGGER.error("Looping dependencies found in {} (referenced in {})!", this, ex.object);
			}

			return false;
		}
	}

	@Override
	protected void verifyDependenciesInternal(long original, int depth) {
		_verify(original, new LongOpenHashSet(), 0);
	}

	private void _verify(long original, LongSet visited, int depth) {
		if (visited.add(id)) {
			if (depth >= 1000) {
				throw new DependencyDepthException(this);
			}
			for (QuestObject dependency : dependencies) {
				if (dependency.id == original) {
					throw new DependencyLoopException(this);
				}
				if (dependency instanceof Quest q) {
					q._verify(original, visited, depth + 1);
				} else if (dependency instanceof Chapter c) {
					c.getQuests().forEach(q -> q._verify(original, visited, depth + 1));
				}
			}
		}
	}

	@Override
	public Set<RecipeModHelper.Components> componentsToRefresh() {
		return EnumSet.of(RecipeModHelper.Components.QUESTS);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void editedFromGUI() {
		QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);

		if (gui != null) {
			gui.refreshChapterPanel();
			gui.refreshQuestPanel();
			gui.refreshViewQuestPanel();
		}
	}

	/**
	 * Get a collection of dependent quest ID's; quests which can't be progressed until this quest is completed.
	 * @return a collection of quest objects, checked for validity
	 */
	public Collection<QuestObject> getDependants() {
		return dependantIDs.stream()
				.map(id -> getQuestFile().get(id))
				.filter(q -> q != null && q.isValid())
				.toList();
	}

	public boolean resetProgressIfRepeatable(TeamData data, UUID player) {
		if (canBeRepeated() && rewards.stream().allMatch(r -> data.isRewardClaimed(player, r))) {
			forceProgress(data, new ProgressChange(this, player));
			return true;
		}
		return false;
	}

	@Override
	public Collection<? extends QuestObject> getChildren() {
		return tasks;
	}

	@Override
	public boolean isCompletedRaw(TeamData data) {
		return data.canStartTasks(this) && super.isCompletedRaw(data);
	}

	@Override
	public boolean hasUnclaimedRewardsRaw(TeamData teamData, UUID player) {
		if (teamData.isCompleted(this)) {
			for (Reward reward : rewards) {
				if (!teamData.isRewardBlocked(reward) && teamData.getClaimType(player, reward).canClaim()) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean ignoreRewardBlocking() {
		return ignoreRewardBlocking;
	}

	public void writeTasks(CompoundTag tag, HolderLookup.Provider provider) {
		ListTag t = new ListTag();
		for (Task task : tasks) {
			TaskType type = task.getType();
			SNBTCompoundTag nbt3 = new SNBTCompoundTag();
			nbt3.putString("id", task.getCodeString());
			nbt3.putString("type", type.getTypeForNBT());
			task.writeData(nbt3, provider);
			t.add(nbt3);
		}
		tag.put("tasks", t);
	}

	public void writeRewards(CompoundTag tag, HolderLookup.Provider provider) {
		ListTag r = new ListTag();
		for (Reward reward : rewards) {
			RewardType type = reward.getType();
			SNBTCompoundTag nbt3 = new SNBTCompoundTag();
			nbt3.putString("id", reward.getCodeString());
			nbt3.putString("type", type.getTypeForNBT());
			reward.writeData(nbt3, provider);
			r.add(nbt3);
		}
		tag.put("rewards", r);
	}

	public boolean hasDependencies() {
		return !dependencies.isEmpty();
	}

	public Stream<QuestObject> streamDependencies() {
		return dependencies.stream();
	}

	private void addDependency(long id) {
		if (getQuestFile().get(id) instanceof QuestObject q) {
			addDependency(q);
		}
	}

	public void addDependency(QuestObject object) {
		dependencies.add(object);
		if (object instanceof Quest q) {
			q.addDependant(id);
		}
	}

	public void removeDependency(QuestObject object) {
		dependencies.remove(object);
		if (object instanceof Quest q) {
			q.removeDependant(id);
			depControlPoints.remove(id);
		}
	}

	public void removeInvalidDependencies() {
		Iterator<QuestObject> iter = dependencies.iterator();
		while (iter.hasNext()) {
			QuestObject qo = iter.next();
			if (qo == null || !qo.isValid() || qo == this) {
				iter.remove();
				if (qo instanceof Quest q) {
					q.removeDependant(id);
				}
			}
		}
	}

	public void clearDependencies() {
		dependencies.forEach(qo -> {
			if (qo instanceof Quest q) {
				q.removeDependant(id);
			}
		});
		dependencies.clear();
		depControlPoints.clear();
	}

	private void addDependant(long id) {
		dependantIDs.add(id);
	}

	private void removeDependant(long id) {
		dependantIDs.remove(id);
	}

	public boolean allTasksCompleted(TeamData teamData) {
		return tasks.stream().allMatch(task -> teamData.getProgress(task) >= task.getMaxProgress());
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean hideDetailsUntilStartable() {
		return hideDetailsUntilStartable.get(chapter.hideQuestDetailsUntilStartable());
	}

	public void addTask(Task task) {
		tasks.add(task);
	}

	public void removeTask(Task task) {
		tasks.remove(task);
	}

	public void addReward(Reward reward) {
		rewards.add(reward);
	}

	public void removeReward(Reward reward) {
		rewards.remove(reward);
	}

	public void setTaskList(List<Task> tasks) {
		this.tasks.clear();
		this.tasks.addAll(tasks);
	}

	public void setRewardList(List<Reward> rewards) {
		this.rewards.clear();
		this.rewards.addAll(rewards);
	}

	/**
	 * {@return the number of seconds until the quest can be repeated, counting from when all rewards have been claimed}
	 */
	public int getRepeatCooldown() {
		return repeatCooldown;
	}

	@FunctionalInterface
	private interface DependencyChecker {
		default boolean check(QuestObject questObject) {
			return questObject.isValid() && check0(questObject);
		}
		boolean check0(QuestObject questObject);
	}

	private boolean checkDependencies(DependencyChecker checker) {
		if (minRequiredDependencies > 0) {
			return streamDependencies()
					.filter(checker::check)
					.limit(minRequiredDependencies)
					.count() == minRequiredDependencies;
		} else if (dependencyRequirement.needOnlyOne()) {
			return streamDependencies().anyMatch(checker::check);
		} else {
			return streamDependencies().allMatch(checker::check);
		}
	}

	public boolean areDependenciesComplete(TeamData teamData) {
		return checkDependencies(dep -> dependencyRequirement.needCompletion() ? teamData.isCompleted(dep) : teamData.isStarted(dep));
	}

	public boolean areDependenciesVisible(TeamData teamData) {
		return checkDependencies(dep -> dep.isVisible(teamData));
	}

	public List<Pair<Integer,Integer>> buildDescriptionIndex() {
		List<Pair<Integer,Integer>> index = new ArrayList<>();

		List<String> rawDescription = getRawDescription();

		int l1 = 0;
		for (int l2 = l1; l2 < rawDescription.size(); l2++) {
			if (rawDescription.get(l2).equals(PAGEBREAK_CODE)) {
				index.add(Pair.of(l1, l2 - 1));
				l1 = l2 + 1;
			}
		}
		if (l1 < rawDescription.size()) {
			index.add(Pair.of(l1, rawDescription.size() - 1));
		}

		return index;
	}

	public Optional<Pair<Vec2d, Vec2d>> getBezierControlPoints(Quest other) {
		return Optional.ofNullable(depControlPoints.get(other.getId()));
	}

	public Map<Long, Pair<Vec2d, Vec2d>> getBezierControlPoints() {
		return depControlPoints;
	}

	public void setBezierControlPoint(Quest other, int index, double newX, double newY) {
		var current= depControlPoints.getOrDefault(other.getId(),
				Pair.of(new Vec2d(other.getX(), other.getY()), new Vec2d(getX(), getY()))
		);
		depControlPoints.put(other.getId(), Pair.of(
				index == 0 ? new Vec2d(newX, newY) : current.getFirst(),
				index == 1 ? new Vec2d(newX, newY) : current.getSecond()
		));
		getQuestFile().markDirty();
	}

	public void setBezierControlPoints(Quest other, @Nullable Pair<Vec2d, Vec2d> controlPoints) {
		if (controlPoints == null) {
			depControlPoints.remove(other.getId());
		} else {
			depControlPoints.put(other.id, controlPoints);
		}
		getQuestFile().markDirty();
	}

	@Override
	public Quest getRelatedQuest() {
		return this;
	}

	@Override
	public boolean isQuestObjectExcluded(TeamData teamData) {
		if (dependencies.isEmpty()) {
			return false;
		}

		int nValidPaths = 0;
		int nQuestDeps = 0;

		for (QuestObject qo : dependencies) {
			if (qo instanceof Quest dependency) {
				nQuestDeps++;

				// Check if this dependency is blocked by maxCompletableDeps
				boolean blockedByMaxCompletable = false;
				if (!dependency.dependantIDs.isEmpty() && dependency.maxCompletableDeps > 0) {
					long completed = dependency.getDependants().stream().filter(teamData::isCompleted).count();
					if (completed >= dependency.maxCompletableDeps && !teamData.isCompleted(this)) {
						blockedByMaxCompletable = true;
					}
				}

				if (blockedByMaxCompletable) {
					continue;
				}

				if (teamData.isCompleted(dependency)) {
					nValidPaths++;
					continue;
				}

				// note: isExcludedByOtherQuestline effectively recurses, but results are cached in TeamData
				if (!teamData.isExcludedByOtherQuestline(dependency)) {
					nValidPaths++;
				}
			}
		}

		if (nQuestDeps == 0) {
			return false;
		}

		int requiredPaths;
		if (minRequiredDependencies > 0) {
			requiredPaths = minRequiredDependencies;
		} else if (dependencyRequirement.needOnlyOne()) {
			requiredPaths = 1;
		} else {
			requiredPaths = nQuestDeps;
		}

		return nValidPaths < requiredPaths;
	}

	public boolean isExclusiveQuest() {
		return dependencies.stream().anyMatch(qo -> qo instanceof Quest q && q.maxCompletableDeps > 0);
	}

	private <T> void moveItem(List<T> list, T item, int direction) {
		int currentIndex = list.indexOf(item);
		int targetIndex = currentIndex + direction;

		if (currentIndex >= 0 && targetIndex >= 0 && targetIndex < list.size()) {
			T targetItem = list.get(targetIndex);
			list.set(targetIndex, item);
			list.set(currentIndex, targetItem);
		}

		getQuestFile().markDirty();
	}

	public void moveTask(Task task, boolean moveRight) {
		moveItem(tasks, task, moveRight ? 1 : -1);
	}

	public void moveReward(Reward reward, boolean moveRight) {
		moveItem(rewards, reward, moveRight ? 1 : -1);
	}

	private class FallbackVisualPreset implements VisualPreset {
		@Override
		public String shapeName() {
			return shape;
		}

		@Override
		public double size() {
			return size;
		}
	}
}
