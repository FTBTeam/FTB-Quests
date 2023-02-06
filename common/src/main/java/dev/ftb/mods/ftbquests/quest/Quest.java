package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.config.*;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.ClientUtils;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.ftb.mods.ftbquests.events.ObjectStartedEvent;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.gui.MultilineTextEditorScreen;
import dev.ftb.mods.ftbquests.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.integration.FTBQuestsJEIHelper;
import dev.ftb.mods.ftbquests.net.DisplayCompletionToastMessage;
import dev.ftb.mods.ftbquests.net.MoveMovableMessage;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardClaimType;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import dev.ftb.mods.ftbquests.util.NetUtils;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import dev.ftb.mods.ftbquests.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author LatvianModder
 */
public final class Quest extends QuestObject implements Movable {

	public Chapter chapter;
	public String subtitle;
	public double x, y;
	public Tristate hide;
	public String shape;
	public final List<String> description;
	private final List<QuestObject> dependencies;
	public final List<Task> tasks;
	public final List<Reward> rewards;
	public DependencyRequirement dependencyRequirement;
	public String guidePage;
	public Tristate hideDependencyLines;
	public int minRequiredDependencies;
	public Tristate hideTextUntilComplete;
	public Tristate disableJEI;
	public Tristate hideDetailsUntilStartable;
	public double size;
	public boolean optional;
	public int minWidth;
	public boolean canRepeat;
	public boolean invisible;  // invisible to players (not the same as hidden!)
	public int invisibleUntilTasks;  // invisible until at least X number of tasks have been completed

	private Component cachedSubtitle = null;
	private Component[] cachedDescription = null;
	private boolean ignoreRewardBlocking;
	private ProgressionMode progressionMode;
	private final Set<Long> dependantIDs;

	public Quest(Chapter c) {
		chapter = c;
		subtitle = "";
		x = 0;
		y = 0;
		shape = "";
		description = new ArrayList<>(0);
		dependencies = new ArrayList<>(0);
		tasks = new ArrayList<>(1);
		rewards = new ArrayList<>(1);
		guidePage = "";
		hideDependencyLines = Tristate.DEFAULT;
		hide = Tristate.DEFAULT;
		dependencyRequirement = DependencyRequirement.ALL_COMPLETED;
		minRequiredDependencies = 0;
		hideTextUntilComplete = Tristate.DEFAULT;
		disableJEI = Tristate.DEFAULT;
		size = 1D;
		optional = false;
		minWidth = 0;
		canRepeat = false;
		invisible = false;
		invisibleUntilTasks = 0;
		ignoreRewardBlocking = false;
		progressionMode = ProgressionMode.DEFAULT;
		dependantIDs = new HashSet<>();
	}

	@Override
	public QuestObjectType getObjectType() {
		return QuestObjectType.QUEST;
	}

	@Override
	public QuestFile getQuestFile() {
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

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putDouble("x", x);
		nbt.putDouble("y", y);

		if (!shape.isEmpty()) {
			nbt.putString("shape", shape);
		}

		if (!subtitle.isEmpty()) {
			nbt.putString("subtitle", subtitle);
		}

		if (!description.isEmpty()) {
			ListTag array = new ListTag();

			for (String value : description) {
				array.add(StringTag.valueOf(value));
			}

			nbt.put("description", array);
		}

		if (!guidePage.isEmpty()) {
			nbt.putString("guide_page", guidePage);
		}

		hideDependencyLines.write(nbt, "hide_dependency_lines");

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
		}

		hide.write(nbt, "hide");

		if (dependencyRequirement != DependencyRequirement.ALL_COMPLETED) {
			nbt.putString("dependency_requirement", dependencyRequirement.id);
		}

		hideTextUntilComplete.write(nbt,"hide_text_until_complete");

		if (size != 1D) {
			nbt.putDouble("size", size);
		}

		if (optional) {
			nbt.putBoolean("optional", true);
		}

		if (minWidth > 0) {
			nbt.putInt("min_width", minWidth);
		}

		if (canRepeat) {
			nbt.putBoolean("can_repeat", true);
		}

		if (invisible) {
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
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		subtitle = nbt.getString("subtitle");
		x = nbt.getDouble("x");
		y = nbt.getDouble("y");
		shape = nbt.getString("shape");

		if (shape.equals("default")) {
			shape = "";
		}

		description.clear();

		ListTag list = nbt.getList("description", Tag.TAG_STRING);

		for (int k = 0; k < list.size(); k++) {
			description.add(list.getString(k));
		}

		guidePage = nbt.getString("guide_page");
		hideDependencyLines = Tristate.read(nbt, "hide_dependency_lines");
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

		hide = Tristate.read(nbt, "hide");
		dependencyRequirement = DependencyRequirement.NAME_MAP.get(nbt.getString("dependency_requirement"));
		hideTextUntilComplete = Tristate.read(nbt, "hide_text_until_complete");
		size = nbt.contains("size") ? nbt.getDouble("size") : 1D;
		optional = nbt.getBoolean("optional");
		minWidth = nbt.getInt("min_width");
		canRepeat = nbt.getBoolean("can_repeat");
		invisible = nbt.getBoolean("invisible");
		invisibleUntilTasks = nbt.getInt("invisible_until_tasks");
		ignoreRewardBlocking = nbt.getBoolean("ignore_reward_blocking");
		progressionMode = ProgressionMode.NAME_MAP.get(nbt.getString("progression_mode"));
		hideDetailsUntilStartable = Tristate.read(nbt, "hide_details_until_startable");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		int flags = 0;
		flags = Bits.setFlag(flags, 0x01, !subtitle.isEmpty());
		flags = Bits.setFlag(flags, 0x02, !description.isEmpty());
		flags = Bits.setFlag(flags, 0x04, size != 1D);
		flags = Bits.setFlag(flags, 0x08, !guidePage.isEmpty());
		flags = Bits.setFlag(flags, 0x10, ignoreRewardBlocking);
		//implement others
		//flags = Bits.setFlag(flags, 32, !customClick.isEmpty());
		flags = Bits.setFlag(flags, 0x40, canRepeat);
		flags = Bits.setFlag(flags, 0x80, invisible);
		flags = Bits.setFlag(flags, 0x100, optional);
		flags = Bits.setFlag(flags, 0x200, minWidth > 0);
		flags = Bits.setFlag(flags, 0x400, invisibleUntilTasks > 0);
		flags = Bits.setFlag(flags, 0x800, hideDetailsUntilStartable != Tristate.DEFAULT);
		flags = Bits.setFlag(flags, 0x1000, hideDetailsUntilStartable == Tristate.TRUE);
		buffer.writeVarInt(flags);

		hide.write(buffer);
		hideDependencyLines.write(buffer);
		hideTextUntilComplete.write(buffer);

		if (!subtitle.isEmpty()) {
			buffer.writeUtf(subtitle, Short.MAX_VALUE);
		}

		buffer.writeDouble(x);
		buffer.writeDouble(y);
		buffer.writeUtf(shape, Short.MAX_VALUE);

		if (!description.isEmpty()) {
			NetUtils.writeStrings(buffer, description);
		}

		if (!guidePage.isEmpty()) {
			buffer.writeUtf(guidePage, Short.MAX_VALUE);
		}

		buffer.writeVarInt(minRequiredDependencies);
		DependencyRequirement.NAME_MAP.write(buffer, dependencyRequirement);
		buffer.writeVarInt(dependencies.size());

		for (QuestObject d : dependencies) {
			buffer.writeLong(d.invalid ? 0L : d.id);
		}

		if (size != 1D) {
			buffer.writeDouble(size);
		}

		if (minWidth > 0) {
			buffer.writeVarInt(minWidth);
		}

		if (invisibleUntilTasks > 0) {
			buffer.writeVarInt(invisibleUntilTasks);
		}

		ProgressionMode.NAME_MAP.write(buffer, progressionMode);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		int flags = buffer.readVarInt();
		hide = Tristate.read(buffer);
		hideDependencyLines = Tristate.read(buffer);
		hideTextUntilComplete = Tristate.read(buffer);

		subtitle = Bits.getFlag(flags, 0x01) ? buffer.readUtf(Short.MAX_VALUE) : "";
		x = buffer.readDouble();
		y = buffer.readDouble();
		shape = buffer.readUtf(Short.MAX_VALUE);

		if (Bits.getFlag(flags, 0x02)) {
			NetUtils.readStrings(buffer, description);
		} else {
			description.clear();
		}

		guidePage = Bits.getFlag(flags, 0x08) ? buffer.readUtf(Short.MAX_VALUE) : "";
		//customClick = Bits.getFlag(flags, 32) ? data.readString() : "";

		minRequiredDependencies = buffer.readVarInt();
		dependencyRequirement = DependencyRequirement.NAME_MAP.read(buffer);
		clearDependencies();
		int d = buffer.readVarInt();

		for (int i = 0; i < d; i++) {
			QuestObject object = chapter.file.get(buffer.readLong());

			if (object != null) {
				addDependency(object);
			}
		}

		progressionMode = ProgressionMode.NAME_MAP.read(buffer);

		size = Bits.getFlag(flags, 0x04) ? buffer.readDouble() : 1D;
		minWidth = Bits.getFlag(flags, 0x200) ? buffer.readVarInt() : 0;
		ignoreRewardBlocking = Bits.getFlag(flags, 0x10);
		canRepeat = Bits.getFlag(flags, 0x40);
		invisible = Bits.getFlag(flags, 0x80);
		optional = Bits.getFlag(flags, 0x100);
		invisibleUntilTasks = Bits.getFlag(flags, 0x400) ? buffer.readVarInt() : 0;
		hideDetailsUntilStartable = Bits.getFlag(flags, 0x800) ? Bits.getFlag(flags, 0x1000) ? Tristate.TRUE : Tristate.FALSE : Tristate.DEFAULT;
	}

	@Override
	public int getRelativeProgressFromChildren(TeamData data) {
		/*if (data.getTimesCompleted(this) > 0)
		{
			return 100;
		}*/

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
		data.teamData.setStarted(id, data.time);
		ObjectStartedEvent.QUEST.invoker().act(new ObjectStartedEvent.QuestEvent(data.withObject(this)));

		if (!data.teamData.isStarted(chapter)) {
			chapter.onStarted(data.withObject(chapter));
		}
	}

	@Override
	public void onCompleted(QuestProgressEventData<?> data) {
		data.teamData.setCompleted(id, data.time);
		ObjectCompletedEvent.QUEST.invoker().act(new ObjectCompletedEvent.QuestEvent(data.withObject(this)));

		if (!disableToast) {
			for (ServerPlayer player : data.notifiedPlayers) {
				new DisplayCompletionToastMessage(id).sendTo(player);
			}
		}

		if (chapter.isCompletedRaw(data.teamData)) {
			chapter.onCompleted(data.withObject(chapter));
		}

		data.teamData.checkAutoCompletion(this);

		checkForDependantCompletion(data.teamData);
	}

	private void checkForDependantCompletion(TeamData data) {
		getDependants().forEach(questObject -> {
			if (questObject instanceof Quest quest) {
				if (quest.getProgressionMode() == ProgressionMode.FLEXIBLE) {
					if (quest.getDependencies().allMatch(data::isCompleted)) {
						quest.tasks.forEach(task -> {
							if (data.getProgress(task.id) >= task.getMaxProgress()) {
								data.markTaskCompleted(task);
							}
						});
					}
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
	@Environment(EnvType.CLIENT)
	public Component getAltTitle() {
		if (!tasks.isEmpty()) {
			return tasks.get(0).getTitle();
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
		chapter.quests.remove(this);

		List<QuestLink> linksToDel = new ArrayList<>();
		chapter.file.chapterGroups.forEach(cg -> cg.chapters.forEach(c -> c.questLinks.forEach(l -> {
			if (l.linksTo(this)) linksToDel.add(l);
		})));
		linksToDel.forEach(l -> chapter.file.deleteObject(l.id));
	}

	@Override
	public void deleteChildren() {
		for (Task task : tasks) {
			task.deleteChildren();
			task.invalid = true;
		}

		for (Reward reward : rewards) {
			reward.deleteChildren();
			reward.invalid = true;
		}

		tasks.clear();
		rewards.clear();
	}

	@Override
	public void onCreated() {
		chapter.quests.add(this);

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
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addString("subtitle", subtitle, v -> subtitle = v, "");

		StringConfig descType = new StringConfig();
		descType.defaultValue = "";
		config.add("description", new ListConfig<String, StringConfig>(descType) {
			@Override
			public void onClicked(MouseButton button, ConfigCallback callback) {
				new MultilineTextEditorScreen(this, callback).openGui();
			}
		}, description, (t) -> {
			description.clear();
			description.addAll(t);
		}, Collections.emptyList());

		config.addEnum("shape", shape.isEmpty() ? "default" : shape, v -> shape = v.equals("default") ? "" : v, QuestShape.idMapWithDefault);
		config.addTristate("hide", hide, v -> hide = v);
		config.addDouble("size", size, v -> size = v, 1, 0.0625D, 8D);
		config.addDouble("x", x, v -> x = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		config.addDouble("y", y, v -> y = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

		Predicate<QuestObjectBase> depTypes = object -> object != chapter.file && object != chapter && object instanceof QuestObject;// && !(object instanceof Task);

		removeInvalidDependencies();

		config.addBool("can_repeat", canRepeat, v -> canRepeat = v, false);
		config.addList("dependencies", dependencies, new ConfigQuestObject<>(depTypes), null).setNameKey("ftbquests.dependencies");
		config.addEnum("dependency_requirement", dependencyRequirement, v -> dependencyRequirement = v, DependencyRequirement.NAME_MAP);
		config.addInt("min_required_dependencies", minRequiredDependencies, v -> minRequiredDependencies = v, 0, 0, Integer.MAX_VALUE);
		config.addTristate("hide_dependency_lines", hideDependencyLines, v -> hideDependencyLines = v);
		config.addString("guide_page", guidePage, v -> guidePage = v, "");
		config.addTristate("hide_text_until_complete", hideTextUntilComplete, v -> hideTextUntilComplete = v);
		config.addEnum("disable_jei", disableJEI, v -> disableJEI = v, Tristate.NAME_MAP);
		config.addBool("optional", optional, v -> optional = v, false);
		config.addInt("min_width", minWidth, v -> minWidth = v, 0, 0, 3000);
		config.addBool("invisible", invisible, v -> invisible = v, false);
		config.addInt("invisible_until_tasks", invisibleUntilTasks, v -> invisibleUntilTasks = v, 0, 0, Integer.MAX_VALUE);
		config.addBool("ignore_reward_blocking", ignoreRewardBlocking, v -> ignoreRewardBlocking = v, false);
		config.addEnum("progression_mode", progressionMode, v -> progressionMode = v, ProgressionMode.NAME_MAP);
		config.addTristate("hide_details_until_startable", hideDetailsUntilStartable, v -> hideDetailsUntilStartable = v);
	}

	public boolean getHideDependencyLines() {
		return hideDependencyLines.get(chapter.defaultHideDependencyLines);
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
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public double getWidth() {
		return size;
	}

	@Override
	public double getHeight() {
		return size;
	}

	@Override
	public String getShape() {
		return shape.isEmpty() ? chapter.getDefaultQuestShape() : shape;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void move(Chapter to, double x, double y) {
		new MoveMovableMessage(this, to.id, x, y).sendToServer();
	}

	@Override
	public boolean isVisible(TeamData data) {
		if (invisible && !data.isCompleted(this)) {
			if (invisibleUntilTasks == 0 || tasks.stream().filter(data::isCompleted).limit(invisibleUntilTasks).count() < invisibleUntilTasks) {
				return false;
			}
		}

		if (dependencies.isEmpty()) {
			return true;
		}

		if (hide.get(false)) {
			return data.areDependenciesComplete(this);
		}

		return getDependencies().anyMatch(object -> object.isVisible(data));
	}

	@Override
	public void clearCachedData() {
		super.clearCachedData();
		cachedSubtitle = null;
		cachedDescription = null;

		for (Task task : tasks) {
			task.clearCachedData();
		}

		for (Reward reward : rewards) {
			reward.clearCachedData();
		}
	}

	@Environment(EnvType.CLIENT)
	public Component getSubtitle() {
		if (cachedSubtitle != null) {
			return cachedSubtitle;
		}

		cachedSubtitle = TextUtils.parseRawText(subtitle);

		return cachedSubtitle;
	}

	@Environment(EnvType.CLIENT)
	public Component[] getDescription() {
		if (cachedDescription != null) {
			return cachedDescription;
		}

		cachedDescription = new Component[description.size()];

		for (int i = 0; i < cachedDescription.length; i++) {
			cachedDescription[i] = TextUtils.parseRawText(description.get(i));
		}

		return cachedDescription;
	}

	public boolean hasDependency(QuestObject object) {
		if (object.invalid) {
			return false;
		}

		for (QuestObject dependency : dependencies) {
			if (dependency == object) {
				return true;
			}
		}

		return false;
	}

	public boolean verifyDependencies(boolean autofix) {
		try {
			verifyDependenciesInternal(id, 0);
			return true;
		} catch (DependencyDepthException ex) {
			if (autofix) {
				FTBQuests.LOGGER.error("Too deep dependencies found in " + this + " (referenced in " + ex.object + ")! Deleting all dependencies...");
				clearDependencies();
				chapter.file.save();
			} else {
				FTBQuests.LOGGER.error("Too deep dependencies found in " + this + " (referenced in " + ex.object + ")!");
			}

			return false;
		} catch (DependencyLoopException ex) {
			if (autofix) {
				FTBQuests.LOGGER.error("Looping dependencies found in " + this + " (referenced in " + ex.object + ")! Deleting all dependencies...");
				clearDependencies();
				chapter.file.save();
			} else {
				FTBQuests.LOGGER.error("Looping dependencies found in " + this + " (referenced in " + ex.object + ")!");
			}

			return false;
		}
	}

	@Override
	protected void verifyDependenciesInternal(long original, int depth) {
		if (depth >= 1000) {
			throw new DependencyDepthException(this);
		}

		for (QuestObject dependency : dependencies) {
			if (dependency.id == original) {
				throw new DependencyLoopException(this);
			}

			dependency.verifyDependenciesInternal(original, depth + 1);
		}
	}

	@Override
	public int refreshJEI() {
		return FTBQuestsJEIHelper.QUESTS;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void editedFromGUI() {
		QuestScreen gui = ClientUtils.getCurrentGuiAs(QuestScreen.class);

		if (gui != null) {
			gui.questPanel.refreshWidgets();
			gui.viewQuestPanel.refreshWidgets();
		}
	}

	@Override
	public void onMoved(double nx, double ny, long nc) {
		x = nx;
		y = ny;

		if (nc != chapter.id) {
			QuestFile f = getQuestFile();
			Chapter c = f.getChapter(nc);

			if (c != null) {
				chapter.quests.remove(this);
				c.quests.add(this);
				chapter = c;
			}
		}
	}

	public boolean isProgressionIgnored() {
		return canRepeat || optional;
	}

	/**
	 * Get a collection of dependent quest ID's; quests which can't be progressed until this quest is completed.
	 * @return a collection of quest objects, checked for validity
	 */
	public Collection<QuestObject> getDependants() {
		return dependantIDs.stream()
				.map(id -> getQuestFile().get(id))
				.filter(q -> q != null && !q.invalid)
				.toList();
	}

	public void checkRepeatable(TeamData data, UUID player) {
		if (canRepeat && rewards.stream().allMatch(r -> data.isRewardClaimed(player, r))) {
			ProgressChange change = new ProgressChange(data.file);
			change.reset = true;
			change.origin = this;
			change.player = player;
			forceProgress(data, change);
		}
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
				if (!teamData.isRewardBlocked(reward) && teamData.getClaimType(player, reward) == RewardClaimType.CAN_CLAIM) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean ignoreRewardBlocking() {
		return ignoreRewardBlocking;
	}

	public void writeTasks(CompoundTag tag) {
		ListTag t = new ListTag();
		for (Task task : tasks) {
			TaskType type = task.getType();
			SNBTCompoundTag nbt3 = new SNBTCompoundTag();
			nbt3.putString("id", task.getCodeString());
			nbt3.putString("type", type.getTypeForNBT());
			task.writeData(nbt3);
			t.add(nbt3);
		}
		tag.put("tasks", t);
	}

	public void writeRewards(CompoundTag tag) {
		ListTag r = new ListTag();
		for (Reward reward : rewards) {
			RewardType type = reward.getType();
			SNBTCompoundTag nbt3 = new SNBTCompoundTag();
			nbt3.putString("id", reward.getCodeString());
			nbt3.putString("type", type.getTypeForNBT());
			reward.writeData(nbt3);
			r.add(nbt3);
		}
		tag.put("rewards", r);
	}

	public boolean hasDependencies() {
		return !dependencies.isEmpty();
	}

	public Stream<QuestObject> getDependencies() {
		return dependencies.stream();
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
		}
	}

	public void removeInvalidDependencies() {
		Iterator<QuestObject> iter = dependencies.iterator();
		while (iter.hasNext()) {
			QuestObject qo = iter.next();
			if (qo == null || qo.invalid || qo == this) {
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

	public boolean hideDetailsUntilStartable() {
		return hideDetailsUntilStartable.get(chapter.hideQuestDetailsUntilStartable());
	}
}
