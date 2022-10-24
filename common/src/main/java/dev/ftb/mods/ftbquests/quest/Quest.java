package dev.ftb.mods.ftbquests.quest;

import com.google.gson.JsonParseException;
import dev.ftb.mods.ftblibrary.config.*;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.ClientTextComponentUtils;
import dev.ftb.mods.ftblibrary.util.ClientUtils;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.ftb.mods.ftbquests.events.ObjectStartedEvent;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.gui.MultilineTextEditorScreen;
import dev.ftb.mods.ftbquests.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.integration.FTBQuestsJEIHelper;
import dev.ftb.mods.ftbquests.net.DisplayCompletionToastMessage;
import dev.ftb.mods.ftbquests.net.MoveQuestMessage;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardClaimType;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import dev.ftb.mods.ftbquests.util.NetUtils;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public final class Quest extends QuestObject implements Movable {
	private static final Pattern JSON_TEXT_PAT = Pattern.compile("^[{\\[]\\s*\"");

	public Chapter chapter;
	public String subtitle;
	public double x, y;
	public Tristate hide;
	public String shape;
	public final List<String> description;
	public final List<QuestObject> dependencies;
	public final List<Task> tasks;
	public final List<Reward> rewards;
	public DependencyRequirement dependencyRequirement;
	public String guidePage;
	public Tristate hideDependencyLines;
	public int minRequiredDependencies;
	public Tristate hideTextUntilComplete;
	public Tristate disableJEI;
	public double size;
	public boolean optional;
	public int minWidth;
	public boolean canRepeat;
	public boolean invisible;  // invisible to players (not the same as hidden!)
	public int invisibleUntilTasks;  // invisible until at least X number of tasks have been completed

	private Component cachedSubtitle = null;
	private Component[] cachedDescription = null;

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

		if (hideDependencyLines != Tristate.DEFAULT) {
			nbt.putBoolean("hide_dependency_lines", hideDependencyLines.isTrue());
		}

		if (minRequiredDependencies > 0) {
			nbt.putInt("min_required_dependencies", (byte) minRequiredDependencies);
		}

		removeInvalidDependencies();

		if (!dependencies.isEmpty()) {
			ListTag deps = new ListTag();

			for (QuestObject dep : dependencies) {
				deps.add(StringTag.valueOf(dep.getCodeString()));
			}

			nbt.put("dependencies", deps);
		}

		if (hide != Tristate.DEFAULT) {
			nbt.putBoolean("hide", hide.isTrue());
		}

		if (dependencyRequirement != DependencyRequirement.ALL_COMPLETED) {
			nbt.putString("dependency_requirement", dependencyRequirement.id);
		}

		if (hideTextUntilComplete != Tristate.DEFAULT) {
			nbt.putBoolean("hide_text_until_complete", hideTextUntilComplete.isTrue());
		}

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

		dependencies.clear();

		if (nbt.contains("dependencies", 11)) {
			for (int i : nbt.getIntArray("dependencies")) {
				QuestObject object = chapter.file.get(i);

				if (object != null) {
					dependencies.add(object);
				}
			}
		} else {
			ListTag deps = nbt.getList("dependencies", Tag.TAG_STRING);

			for (int i = 0; i < deps.size(); i++) {
				QuestObject object = chapter.file.get(chapter.file.getID(deps.getString(i)));

				if (object != null) {
					dependencies.add(object);
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
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		int flags = 0;
		flags = Bits.setFlag(flags, 1, !subtitle.isEmpty());
		flags = Bits.setFlag(flags, 2, !description.isEmpty());
		flags = Bits.setFlag(flags, 4, size != 1D);
		flags = Bits.setFlag(flags, 8, !guidePage.isEmpty());
		//implement others
		//flags = Bits.setFlag(flags, 32, !customClick.isEmpty());
		flags = Bits.setFlag(flags, 64, canRepeat);
		flags = Bits.setFlag(flags, 128, invisible);
		flags = Bits.setFlag(flags, 256, optional);
		flags = Bits.setFlag(flags, 512, minWidth > 0);
		flags = Bits.setFlag(flags, 1024, invisibleUntilTasks > 0);
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
			if (d.invalid) {
				buffer.writeLong(0L);
			} else {
				buffer.writeLong(d.id);
			}
		}

		if (size != 1D) {
			buffer.writeDouble(size);
		}

		if (minWidth > 0) {
			buffer.writeVarInt(minWidth);
		}

		buffer.writeBoolean(canRepeat);

		if (invisibleUntilTasks > 0) {
			buffer.writeVarInt(invisibleUntilTasks);
		}
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		int flags = buffer.readVarInt();
		hide = Tristate.read(buffer);
		hideDependencyLines = Tristate.read(buffer);
		hideTextUntilComplete = Tristate.read(buffer);

		subtitle = Bits.getFlag(flags, 1) ? buffer.readUtf(Short.MAX_VALUE) : "";
		x = buffer.readDouble();
		y = buffer.readDouble();
		shape = buffer.readUtf(Short.MAX_VALUE);

		if (Bits.getFlag(flags, 2)) {
			NetUtils.readStrings(buffer, description);
		} else {
			description.clear();
		}

		guidePage = Bits.getFlag(flags, 8) ? buffer.readUtf(Short.MAX_VALUE) : "";
		//customClick = Bits.getFlag(flags, 32) ? data.readString() : "";
		optional = Bits.getFlag(flags, 64);

		minRequiredDependencies = buffer.readVarInt();
		dependencyRequirement = DependencyRequirement.NAME_MAP.read(buffer);
		dependencies.clear();
		int d = buffer.readVarInt();

		for (int i = 0; i < d; i++) {
			QuestObject object = chapter.file.get(buffer.readLong());

			if (object != null) {
				dependencies.add(object);
			}
		}

		size = Bits.getFlag(flags, 4) ? buffer.readDouble() : 1D;
		minWidth = Bits.getFlag(flags, 512) ? buffer.readVarInt() : 0;
		canRepeat = buffer.readBoolean();
		invisible = Bits.getFlag(flags, 128);
		invisibleUntilTasks = Bits.getFlag(flags, 1024) ? buffer.readVarInt() : 0;
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

		for (ChapterGroup group : chapter.file.chapterGroups) {
			for (Chapter chapter : group.chapters) {
				for (Quest quest : chapter.quests) {
					if (quest.dependencies.contains(this)) {
						data.teamData.checkAutoCompletion(quest);
					}
				}
			}
		}
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

		return new TranslatableComponent("ftbquests.unnamed");
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

		dependencies.removeIf(Objects::isNull);
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
	}

	public boolean getHideDependencyLines() {
		return hideDependencyLines.get(chapter.defaultHideDependencyLines);
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
		new MoveQuestMessage(id, to.id, x, y).sendToServer();
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

		for (QuestObject object : dependencies) {
			if (object.isVisible(data)) {
				return true;
			}
		}

		return false;
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

		cachedSubtitle = JSON_TEXT_PAT.matcher(subtitle).find() ?
				rawTextToComponent(subtitle) :
				ClientTextComponentUtils.parse(subtitle);

		return cachedSubtitle;
	}

	@Environment(EnvType.CLIENT)
	public Component[] getDescription() {
		if (cachedDescription != null) {
			return cachedDescription;
		}

		cachedDescription = new Component[description.size()];

		for (int i = 0; i < cachedDescription.length; i++) {
			cachedDescription[i] = JSON_TEXT_PAT.matcher(description.get(i)).find() ?
					rawTextToComponent(description.get(i)) :
					ClientTextComponentUtils.parse(description.get(i));
		}

		return cachedDescription;
	}

	private Component rawTextToComponent(String raw) {
		try {
			return Component.Serializer.fromJson(raw);
		} catch (JsonParseException e) {
			return new TextComponent("ERROR: " + e.getMessage()).withStyle(ChatFormatting.RED);
		}
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

	public void removeInvalidDependencies() {
		if (!dependencies.isEmpty()) {
			dependencies.removeIf(o -> o == null || o.invalid || o == this);
		}
	}

	public boolean verifyDependencies(boolean autofix) {
		try {
			verifyDependenciesInternal(id, 0);
			return true;
		} catch (DependencyDepthException ex) {
			if (autofix) {
				FTBQuests.LOGGER.error("Too deep dependencies found in " + this + " (referenced in " + ex.object + ")! Deleting all dependencies...");
				dependencies.clear();
				chapter.file.save();
			} else {
				FTBQuests.LOGGER.error("Too deep dependencies found in " + this + " (referenced in " + ex.object + ")!");
			}

			return false;
		} catch (DependencyLoopException ex) {
			if (autofix) {
				FTBQuests.LOGGER.error("Looping dependencies found in " + this + " (referenced in " + ex.object + ")! Deleting all dependencies...");
				dependencies.clear();
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

	public void moved(double nx, double ny, long nc) {
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

	public List<QuestObject> getDependants() {
		List<QuestObject> list = new ArrayList<>();

		for (ChapterGroup group : chapter.file.chapterGroups) {
			for (Chapter c : group.chapters) {
				for (Quest q : c.quests) {
					if (q.dependencies.contains(this)) {
						list.add(q);
					}
				}
			}
		}

		return list;
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
				if (teamData.getClaimType(player, reward) == RewardClaimType.CAN_CLAIM) {
					return true;
				}
			}
		}

		return false;
	}
}