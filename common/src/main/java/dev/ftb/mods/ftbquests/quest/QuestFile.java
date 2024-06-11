package dev.ftb.mods.ftbquests.quest;

import dev.architectury.utils.Env;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ItemStackConfig;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.math.MathUtils;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.events.*;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.item.MissingItem;
import dev.ftb.mods.ftbquests.net.DisplayCompletionToastMessage;
import dev.ftb.mods.ftbquests.net.FTBQuestsNetHandler;
import dev.ftb.mods.ftbquests.quest.loot.EntityWeight;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.*;
import dev.ftb.mods.ftbquests.quest.task.*;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.util.NetUtils;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.TeamBase;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class QuestFile extends QuestObject {
	public static int VERSION = 13;

	public final DefaultChapterGroup defaultChapterGroup;
	public final List<ChapterGroup> chapterGroups;
	public final List<RewardTable> rewardTables;
	protected final Map<UUID, TeamData> teamDataMap;

	private final Long2ObjectOpenHashMap<QuestObjectBase> map;
	public final Int2ObjectOpenHashMap<TaskType> taskTypeIds;
	public final Int2ObjectOpenHashMap<RewardType> rewardTypeIds;

	public final List<ItemStack> emergencyItems;
	public int emergencyItemsCooldown;
	public int fileVersion;
	public boolean defaultRewardTeam;
	public boolean defaultTeamConsumeItems;
	public RewardAutoClaim defaultRewardAutoClaim;
	public String defaultQuestShape;
	public boolean defaultQuestDisableJEI;
	public boolean dropLootCrates;
	public final EntityWeight lootCrateNoDrop;
	public boolean disableGui;
	public double gridScale;
	public boolean pauseGame;
	public String lockMessage;
	private ProgressionMode progressionMode;
	public int detectionDelay;

	private List<Task> allTasks;
	private List<Task> submitTasks;
	private List<Task> craftingTasks;

	public QuestFile() {
		id = 1;
		fileVersion = 0;
		defaultChapterGroup = new DefaultChapterGroup(this);
		chapterGroups = new ArrayList<>();
		chapterGroups.add(defaultChapterGroup);
		rewardTables = new ArrayList<>();
		teamDataMap = new HashMap<>();

		map = new Long2ObjectOpenHashMap<>();
		taskTypeIds = new Int2ObjectOpenHashMap<>();
		rewardTypeIds = new Int2ObjectOpenHashMap<>();

		emergencyItems = new ArrayList<>();
		emergencyItemsCooldown = 300;

		defaultRewardTeam = false;
		defaultTeamConsumeItems = false;
		defaultRewardAutoClaim = RewardAutoClaim.DISABLED;
		defaultQuestShape = "circle";
		defaultQuestDisableJEI = false;
		dropLootCrates = false;
		lootCrateNoDrop = new EntityWeight();
		lootCrateNoDrop.passive = 4000;
		lootCrateNoDrop.monster = 600;
		lootCrateNoDrop.boss = 0;
		disableGui = false;
		gridScale = 0.5D;
		pauseGame = false;
		lockMessage = "";
		progressionMode = ProgressionMode.LINEAR;
		detectionDelay = 20;

		allTasks = null;
	}

	public abstract Env getSide();

	public final boolean isServerSide() {
		return getSide() == Env.SERVER;
	}

	@Override
	public QuestFile getQuestFile() {
		return this;
	}

	@Override
	public QuestObjectType getObjectType() {
		return QuestObjectType.FILE;
	}

	public boolean isLoading() {
		return false;
	}

	public boolean canEdit() {
		return false;
	}

	public Path getFolder() {
		throw new IllegalStateException("This quest file doesn't have a folder!");
	}

	public void load() {
		throw new IllegalStateException("This method can only be called from client quest file!");
	}

	@Override
	public int getRelativeProgressFromChildren(TeamData data) {
		int progress = 0;
		int chapters = 0;

		for (ChapterGroup group : chapterGroups) {
			for (Chapter chapter : group.chapters) {
				progress += data.getRelativeProgress(chapter);
				chapters++;
			}
		}

		return getRelativeProgressFromChildren(progress, chapters);
	}

	@Override
	public void onStarted(QuestProgressEventData<?> data) {
		data.teamData.setStarted(id, data.time);
		ObjectStartedEvent.FILE.invoker().act(new ObjectStartedEvent.FileEvent(data.withObject(this)));
	}

	@Override
	public void onCompleted(QuestProgressEventData<?> data) {
		data.teamData.setCompleted(id, data.time);
		ObjectCompletedEvent.FILE.invoker().act(new ObjectCompletedEvent.FileEvent(data.withObject(this)));

		if (!disableToast) {
			for (ServerPlayer player : data.notifiedPlayers) {
				new DisplayCompletionToastMessage(id).sendTo(player);
			}
		}
	}

	@Override
	public void deleteSelf() {
		invalid = true;
	}

	@Override
	public void deleteChildren() {
		for (ChapterGroup group : chapterGroups) {
			for (Chapter chapter : group.chapters) {
				chapter.deleteChildren();
				chapter.invalid = true;
			}
		}

		defaultChapterGroup.chapters.clear();
		chapterGroups.clear();
		chapterGroups.add(defaultChapterGroup);

		for (RewardTable table : rewardTables) {
			table.deleteChildren();
			table.invalid = true;
		}

		rewardTables.clear();
	}

	@Nullable
	public QuestObjectBase getBase(long id) {
		if (id <= 0) {
			return null;
		} else if (id == 1) {
			return this;
		}

		QuestObjectBase object = map.get(id);
		return object == null || object.invalid ? null : object;
	}

	@Nullable
	public QuestObject get(long id) {
		QuestObjectBase object = getBase(id);
		return object instanceof QuestObject ? (QuestObject) object : null;
	}

	@Nullable
	public QuestObjectBase remove(long id) {
		QuestObjectBase object = map.remove(id);

		if (object != null) {
			if (object instanceof QuestObject qo) {
				for (ChapterGroup group : chapterGroups) {
					for (Chapter chapter : group.chapters) {
						for (Quest quest : chapter.getQuests()) {
							quest.removeDependency(qo);
						}
					}
				}
			}

			object.invalid = true;
			refreshIDMap();
			return object;
		}

		return null;
	}

	@Nullable
	public Chapter getChapter(long id) {
		QuestObjectBase object = getBase(id);
		return object instanceof Chapter ? (Chapter) object : null;
	}

	@NotNull
	public Chapter getChapterOrThrow(long id) {
		if (getBase(id) instanceof Chapter c) return c;
		throw new IllegalArgumentException("Unknown chapter ID: c");
	}

	@Nullable
	public Quest getQuest(long id) {
		QuestObjectBase object = getBase(id);
		return object instanceof Quest ? (Quest) object : null;
	}

	@Nullable
	public Task getTask(long id) {
		QuestObjectBase object = getBase(id);
		return object instanceof Task ? (Task) object : null;
	}

	@Nullable
	public Reward getReward(long id) {
		QuestObjectBase object = getBase(id);
		return object instanceof Reward ? (Reward) object : null;
	}

	@Nullable
	public RewardTable getRewardTable(long id) {
		QuestObjectBase object = getBase(id);
		return object instanceof RewardTable ? (RewardTable) object : null;
	}

	@Nullable
	public LootCrate getLootCrate(String id) {
		if (!id.startsWith("#")) {
			for (RewardTable table : rewardTables) {
				if (table.lootCrate != null && table.lootCrate.stringID.equals(id)) {
					return table.lootCrate;
				}
			}
		}

		RewardTable table = getRewardTable(getID(id));
		return table == null ? null : table.lootCrate;
	}

	public ChapterGroup getChapterGroup(long id) {
		QuestObjectBase object = getBase(id);
		return object instanceof ChapterGroup ? (ChapterGroup) object : defaultChapterGroup;
	}

	public void refreshIDMap() {
		clearCachedData();
		map.clear();

		for (ChapterGroup group : chapterGroups) {
			map.put(group.id, group);
		}

		for (RewardTable table : rewardTables) {
			map.put(table.id, table);
		}

		for (ChapterGroup group : chapterGroups) {
			for (Chapter chapter : group.chapters) {
				map.put(chapter.id, chapter);

				for (Quest quest : chapter.getQuests()) {
					map.put(quest.id, quest);

					for (Task task : quest.tasks) {
						map.put(task.id, task);
					}

					for (Reward reward : quest.rewards) {
						map.put(reward.id, reward);
					}
				}

				for (QuestLink link : chapter.getQuestLinks()) {
					map.put(link.id, link);
				}
			}
		}

		clearCachedData();
	}

	public QuestObjectBase create(QuestObjectType type, long parent, CompoundTag extra) {
		switch (type) {
			case CHAPTER -> {
				return new Chapter(this, getChapterGroup(extra.getLong("group")));
			}
			case QUEST -> {
				Chapter chapter = getChapter(parent);
				if (chapter != null) {
					return new Quest(chapter);
				}
				throw new IllegalArgumentException("Parent chapter not found!");
			}
			case QUEST_LINK -> {
				Chapter chapter = getChapter(parent);
				if (chapter != null) {
					return new QuestLink(chapter, 0L);
				}
				throw new IllegalArgumentException("Parent chapter not found!");
			}
			case TASK -> {
				Quest quest = getQuest(parent);
				if (quest != null) {
					Task task = TaskType.createTask(quest, extra.getString("type"));
					if (task != null) {
						return task;
					}
					throw new IllegalArgumentException("Unknown task type!");
				}
				throw new IllegalArgumentException("Parent quest not found!");
			}
			case REWARD -> {
				Quest quest = getQuest(parent);
				if (quest != null) {
					Reward reward = RewardType.createReward(quest, extra.getString("type"));
					if (reward != null) {
						return reward;
					}
					throw new IllegalArgumentException("Unknown reward type!");
				}
				throw new IllegalArgumentException("Parent quest not found!");
			}
			case REWARD_TABLE -> {
				return new RewardTable(this);
			}
			case CHAPTER_GROUP -> {
				return new ChapterGroup(this);
			}
			default -> throw new IllegalArgumentException("Unknown type: " + type);
		}
	}

	@Override
	public final void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putBoolean("default_reward_team", defaultRewardTeam);
		nbt.putBoolean("default_consume_items", defaultTeamConsumeItems);
		nbt.putString("default_autoclaim_rewards", defaultRewardAutoClaim.id);
		nbt.putString("default_quest_shape", defaultQuestShape);
		nbt.putBoolean("default_quest_disable_jei", defaultQuestDisableJEI);

		if (!emergencyItems.isEmpty()) {
			ListTag list = new ListTag();

			for (ItemStack stack : emergencyItems) {
				list.add(MissingItem.writeItem(stack));
			}

			nbt.put("emergency_items", list);
		}

		nbt.putInt("emergency_items_cooldown", emergencyItemsCooldown);
		nbt.putBoolean("drop_loot_crates", dropLootCrates);

		SNBTCompoundTag lootCrateNoDropTag = new SNBTCompoundTag();
		lootCrateNoDrop.writeData(lootCrateNoDropTag);
		nbt.put("loot_crate_no_drop", lootCrateNoDropTag);
		nbt.putBoolean("disable_gui", disableGui);
		nbt.putDouble("grid_scale", gridScale);
		nbt.putBoolean("pause_game", pauseGame);
		nbt.putString("lock_message", lockMessage);
		nbt.putString("progression_mode", progressionMode.getId());
		nbt.putInt("detection_delay", detectionDelay);
	}

	@Override
	public final void readData(CompoundTag nbt) {
		super.readData(nbt);
		defaultRewardTeam = nbt.getBoolean("default_reward_team");
		defaultTeamConsumeItems = nbt.getBoolean("default_consume_items");
		defaultRewardAutoClaim = RewardAutoClaim.NAME_MAP_NO_DEFAULT.get(nbt.getString("default_autoclaim_rewards"));
		defaultQuestShape = nbt.getString("default_quest_shape");

		if (defaultQuestShape.equals("default")) {
			defaultQuestShape = "";
		}

		defaultQuestDisableJEI = nbt.getBoolean("default_quest_disable_jei");
		emergencyItems.clear();

		ListTag emergencyItemsTag = nbt.getList("emergency_items", Tag.TAG_COMPOUND);

		for (int i = 0; i < emergencyItemsTag.size(); i++) {
			ItemStack stack = MissingItem.readItem(emergencyItemsTag.getCompound(i));

			if (!stack.isEmpty()) {
				emergencyItems.add(stack);
			}
		}

		emergencyItemsCooldown = nbt.getInt("emergency_items_cooldown");
		dropLootCrates = nbt.getBoolean("drop_loot_crates");

		if (nbt.contains("loot_crate_no_drop")) {
			lootCrateNoDrop.readData(nbt.getCompound("loot_crate_no_drop"));
		}

		disableGui = nbt.getBoolean("disable_gui");
		gridScale = nbt.contains("grid_scale") ? nbt.getDouble("grid_scale") : 0.5D;
		pauseGame = nbt.getBoolean("pause_game");
		lockMessage = nbt.getString("lock_message");
		progressionMode = ProgressionMode.NAME_MAP_NO_DEFAULT.get(nbt.getString("progression_mode"));
		if (nbt.contains("detection_delay")) {
			detectionDelay = nbt.getInt("detection_delay");
		}
	}

	public final void writeDataFull(Path folder) {
		boolean prev = false;
		try {
			// Sorting keys ensure consistent sort order in the saved quest file
			// Since questbook data is commonly stored under version control, this minimizes extraneous
			//  version control changes stemming from unpredictable hashmap key ordering
			prev = SNBT.setShouldSortKeysOnWrite(true);

			SNBTCompoundTag fileNBT = new SNBTCompoundTag();
			fileNBT.putInt("version", VERSION);
			writeData(fileNBT);
			SNBT.write(folder.resolve("data.snbt"), fileNBT);

			for (ChapterGroup group : chapterGroups) {
				for (int ci = 0; ci < group.chapters.size(); ci++) {
					Chapter chapter = group.chapters.get(ci);
					SNBTCompoundTag chapterNBT = new SNBTCompoundTag();
					chapterNBT.putString("id", chapter.getCodeString());
					chapterNBT.putString("group", group.isDefaultGroup() ? "" : group.getCodeString());
					chapterNBT.putInt("order_index", ci);
					chapter.writeData(chapterNBT);

					ListTag questList = new ListTag();
					for (Quest quest : chapter.getQuests()) {
						if (!quest.invalid) {
							SNBTCompoundTag questNBT = new SNBTCompoundTag();
							quest.writeData(questNBT);
							questNBT.putString("id", quest.getCodeString());
							if (!quest.tasks.isEmpty()) {
								quest.writeTasks(questNBT);
							}
							if (!quest.rewards.isEmpty()) {
								quest.writeRewards(questNBT);
							}
							questList.add(questNBT);
						}
					}
					chapterNBT.put("quests", questList);

					ListTag linkList = new ListTag();
					for (QuestLink link : chapter.getQuestLinks()) {
						if (link.getQuest().isPresent()) {
							SNBTCompoundTag linkNBT = new SNBTCompoundTag();
							link.writeData(linkNBT);
							linkNBT.putString("id", link.getCodeString());
							linkList.add(linkNBT);
						}
					}
					chapterNBT.put("quest_links", linkList);

					SNBT.write(folder.resolve("chapters/" + chapter.getFilename() + ".snbt"), chapterNBT);
				}
			}

			for (int ri = 0; ri < rewardTables.size(); ri++) {
				RewardTable table = rewardTables.get(ri);
				SNBTCompoundTag tableNBT = new SNBTCompoundTag();
				tableNBT.putString("id", table.getCodeString());
				tableNBT.putInt("order_index", ri);
				table.writeData(tableNBT);
				SNBT.write(folder.resolve("reward_tables/" + table.getFilename() + ".snbt"), tableNBT);
			}

			ListTag chapterGroupTag = new ListTag();

			for (ChapterGroup group : chapterGroups) {
				if (!group.isDefaultGroup()) {
					SNBTCompoundTag groupTag = new SNBTCompoundTag();
					groupTag.singleLine();
					groupTag.putString("id", group.getCodeString());
					group.writeData(groupTag);
					chapterGroupTag.add(groupTag);
				}
			}

			SNBTCompoundTag groupNBT = new SNBTCompoundTag();
			groupNBT.put("chapter_groups", chapterGroupTag);
			SNBT.write(folder.resolve("chapter_groups.snbt"), groupNBT);
		} finally {
			SNBT.setShouldSortKeysOnWrite(prev);
		}
	}

	public final void readDataFull(Path folder) {
		clearCachedData();
		map.clear();
		defaultChapterGroup.chapters.clear();
		chapterGroups.clear();
		chapterGroups.add(defaultChapterGroup);
		rewardTables.clear();

		MutableInt chapterCounter = new MutableInt();
		MutableInt questCounter = new MutableInt();

		final Long2ObjectOpenHashMap<CompoundTag> dataCache = new Long2ObjectOpenHashMap<>();
		CompoundTag fileNBT = SNBT.read(folder.resolve("data.snbt"));

		if (fileNBT != null) {
			fileVersion = fileNBT.getInt("version");
			map.put(1, this);
			readData(fileNBT);
		}

		Path groupsFile = folder.resolve("chapter_groups.snbt");

		if (Files.exists(groupsFile)) {
			CompoundTag chapterGroupsTag = SNBT.read(groupsFile);

			if (chapterGroupsTag != null) {
				ListTag groupListTag = chapterGroupsTag.getList("chapter_groups", Tag.TAG_COMPOUND);

				for (int i = 0; i < groupListTag.size(); i++) {
					CompoundTag groupNBT = groupListTag.getCompound(i);
					ChapterGroup chapterGroup = new ChapterGroup(this);
					chapterGroup.id = readID(groupNBT.get("id"));
					map.put(chapterGroup.id, chapterGroup);
					dataCache.put(chapterGroup.id, groupNBT);
					chapterGroups.add(chapterGroup);
				}
			}
		}

		Path chaptersFolder = folder.resolve("chapters");

		Long2IntOpenHashMap objectOrderMap = new Long2IntOpenHashMap();
		objectOrderMap.defaultReturnValue(-1);

		if (Files.exists(chaptersFolder)) {
			try (Stream<Path> s = Files.list(chaptersFolder)) {
				s.filter(path -> path.toString().endsWith(".snbt")).forEach(path -> {
					CompoundTag chapterNBT = SNBT.read(path);

					if (chapterNBT != null) {
						Chapter chapter = new Chapter(this, getChapterGroup(getID(chapterNBT.get("group"))));
						chapter.id = readID(chapterNBT.get("id"));
						chapter.filename = path.getFileName().toString().replace(".snbt", "");
						objectOrderMap.put(chapter.id, chapterNBT.getInt("order_index"));
						map.put(chapter.id, chapter);
						dataCache.put(chapter.id, chapterNBT);
						chapter.group.chapters.add(chapter);

						ListTag questList = chapterNBT.getList("quests", Tag.TAG_COMPOUND);

						for (int i = 0; i < questList.size(); i++) {
							CompoundTag questNBT = questList.getCompound(i);
							Quest quest = new Quest(chapter);
							quest.id = readID(questNBT.get("id"));
							map.put(quest.id, quest);
							dataCache.put(quest.id, questNBT);
							chapter.getQuests().add(quest);

							ListTag taskList = questNBT.getList("tasks", Tag.TAG_COMPOUND);

							for (int j = 0; j < taskList.size(); j++) {
								CompoundTag taskNBT = taskList.getCompound(j);
								Task task = TaskType.createTask(quest, taskNBT.getString("type"));

								if (task == null) {
									task = new CustomTask(quest);
									task.title = "Unknown type: " + taskNBT.getString("type");
								}

								task.id = readID(taskNBT.get("id"));
								map.put(task.id, task);
								dataCache.put(task.id, taskNBT);
								quest.tasks.add(task);
							}

							ListTag rewardList = questNBT.getList("rewards", Tag.TAG_COMPOUND);

							for (int j = 0; j < rewardList.size(); j++) {
								CompoundTag rewardNBT = rewardList.getCompound(j);
								Reward reward = RewardType.createReward(quest, rewardNBT.getString("type"));

								if (reward == null) {
									reward = new CustomReward(quest);
									reward.title = "Unknown type: " + rewardNBT.getString("type");
								}

								reward.id = readID(rewardNBT.get("id"));
								map.put(reward.id, reward);
								dataCache.put(reward.id, rewardNBT);
								quest.rewards.add(reward);
							}

							questCounter.increment();
						}

						ListTag questLinks = chapterNBT.getList("quest_links", Tag.TAG_COMPOUND);
						for (int i = 0; i < questLinks.size(); i++) {
							CompoundTag linkNBT = questLinks.getCompound(i);
							QuestLink link = new QuestLink(chapter, readID(linkNBT.get("linked_quest")));
							link.id = readID(linkNBT.get("id"));
							chapter.getQuestLinks().add(link);
							map.put(link.id, link);
							dataCache.put(link.id, linkNBT);
						}

						chapterCounter.increment();
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Path rewardTableFolder = folder.resolve("reward_tables");

		if (Files.exists(rewardTableFolder)) {
			try (Stream<Path> s = Files.list(rewardTableFolder)) {
				s.filter(path -> path.toString().endsWith(".snbt")).forEach(path -> {
					CompoundTag tableNBT = SNBT.read(path);

					if (tableNBT != null) {
						RewardTable table = new RewardTable(this);
						table.id = readID(tableNBT.get("id"));
						table.filename = path.getFileName().toString().replace(".snbt", "");
						objectOrderMap.put(table.id, tableNBT.getInt("order_index"));
						map.put(table.id, table);
						dataCache.put(table.id, tableNBT);
						rewardTables.add(table);
					}
				});
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		for (QuestObjectBase object : map.values()) {
			CompoundTag data = dataCache.get(object.id);

			if (data != null) {
				object.readData(data);
			}
		}

		for (ChapterGroup group : chapterGroups) {
			group.chapters.sort(Comparator.comparingInt(c -> objectOrderMap.get(c.id)));

			for (Chapter chapter : group.chapters) {
				for (Quest quest : chapter.getQuests()) {
					quest.removeInvalidDependencies();
				}
			}
		}

		rewardTables.sort(Comparator.comparingInt(c -> objectOrderMap.get(c.id)));
		updateLootCrates();

        /*
		for (Chapter chapter : chapters)
		{
			for (Quest quest : chapter.quests)
			{
				quest.verifyDependencies(true);
			}
		}
		*/

		for (QuestObjectBase object : getAllObjects()) {
			if (object instanceof CustomTask) {
				CustomTaskEvent.EVENT.invoker().act(new CustomTaskEvent((CustomTask) object));
			}
		}

		if (fileVersion != VERSION) {
			save();
		}

		FTBQuests.LOGGER.info("Loaded " + chapterGroups.size() + " chapter groups, " + chapterCounter + " chapters, " + questCounter + " quests, " + rewardTables.size() + " reward tables");
	}

	public void updateLootCrates() {
		LootCrate.LOOT_CRATES.clear();

		for (RewardTable table : rewardTables) {
			if (table.lootCrate != null) {
				LootCrate.LOOT_CRATES.put(table.lootCrate.getStringID(), table.lootCrate);
			}
		}

		if (!isServerSide()) {
			FTBQuests.getRecipeModHelper().refreshAll(RecipeModHelper.Components.LOOT_CRATES);
		}

		FTBQuests.LOGGER.debug("Updated " + LootCrate.LOOT_CRATES.size() + " loot crates");
	}

	public void save() {
	}

	@Override
	public final void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		NetUtils.write(buffer, emergencyItems, FTBQuestsNetHandler::writeItemType);
		buffer.writeVarInt(emergencyItemsCooldown);
		buffer.writeBoolean(defaultRewardTeam);
		buffer.writeBoolean(defaultTeamConsumeItems);
		RewardAutoClaim.NAME_MAP_NO_DEFAULT.write(buffer, defaultRewardAutoClaim);
		buffer.writeUtf(defaultQuestShape, Short.MAX_VALUE);
		buffer.writeBoolean(defaultQuestDisableJEI);
		buffer.writeBoolean(dropLootCrates);
		lootCrateNoDrop.writeNetData(buffer);
		buffer.writeBoolean(disableGui);
		buffer.writeDouble(gridScale);
		buffer.writeBoolean(pauseGame);
		buffer.writeUtf(lockMessage, Short.MAX_VALUE);
		ProgressionMode.NAME_MAP_NO_DEFAULT.write(buffer, progressionMode);
		buffer.writeVarInt(detectionDelay);
	}

	@Override
	public final void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		NetUtils.read(buffer, emergencyItems, FTBQuestsNetHandler::readItemType);
		emergencyItemsCooldown = buffer.readVarInt();
		defaultRewardTeam = buffer.readBoolean();
		defaultTeamConsumeItems = buffer.readBoolean();
		defaultRewardAutoClaim = RewardAutoClaim.NAME_MAP_NO_DEFAULT.read(buffer);
		defaultQuestShape = buffer.readUtf(Short.MAX_VALUE);
		defaultQuestDisableJEI = buffer.readBoolean();
		dropLootCrates = buffer.readBoolean();
		lootCrateNoDrop.readNetData(buffer);
		disableGui = buffer.readBoolean();
		gridScale = buffer.readDouble();
		pauseGame = buffer.readBoolean();
		lockMessage = buffer.readUtf(Short.MAX_VALUE);
		progressionMode = ProgressionMode.NAME_MAP_NO_DEFAULT.read(buffer);
		detectionDelay = buffer.readVarInt();
	}

	public final void writeNetDataFull(FriendlyByteBuf buffer) {
		int pos = buffer.writerIndex();

		buffer.writeVarInt(TaskTypes.TYPES.size());
		for (TaskType type : TaskTypes.TYPES.values()) {
			buffer.writeResourceLocation(type.id);
			buffer.writeVarInt(type.intId);
		}

		buffer.writeVarInt(RewardTypes.TYPES.size());
		for (RewardType type : RewardTypes.TYPES.values()) {
			buffer.writeResourceLocation(type.id);
			buffer.writeVarInt(type.intId);
		}

		writeNetData(buffer);

		buffer.writeVarInt(rewardTables.size());
		for (RewardTable table : rewardTables) {
			buffer.writeLong(table.id);
		}

		buffer.writeVarInt(chapterGroups.size() - 1);
		for (ChapterGroup group : chapterGroups) {
			if (!group.isDefaultGroup()) {
				buffer.writeLong(group.id);
			}
		}

		for (ChapterGroup group : chapterGroups) {
			buffer.writeVarInt(group.chapters.size());

			for (Chapter chapter : group.chapters) {
				buffer.writeLong(chapter.id);
				buffer.writeVarInt(chapter.getQuests().size());

				for (Quest quest : chapter.getQuests()) {
					buffer.writeLong(quest.id);

					buffer.writeVarInt(quest.tasks.size());
					for (Task task : quest.tasks) {
						buffer.writeVarInt(task.getType().intId);
						buffer.writeLong(task.id);
					}

					buffer.writeVarInt(quest.rewards.size());
					for (Reward reward : quest.rewards) {
						buffer.writeVarInt(reward.getType().intId);
						buffer.writeLong(reward.id);
					}
				}

				buffer.writeVarInt(chapter.getQuestLinks().size());
				for (QuestLink questLink : chapter.getQuestLinks()) {
					buffer.writeLong(questLink.id);
				}
			}
		}

		for (RewardTable table : rewardTables) {
			table.writeNetData(buffer);
		}

		for (ChapterGroup group : chapterGroups) {
			if (!group.isDefaultGroup()) {
				group.writeNetData(buffer);
			}
		}

		for (ChapterGroup group : chapterGroups) {
			for (Chapter chapter : group.chapters) {
				chapter.writeNetData(buffer);

				for (Quest quest : chapter.getQuests()) {
					quest.writeNetData(buffer);

					for (Task task : quest.tasks) {
						task.writeNetData(buffer);
					}

					for (Reward reward : quest.rewards) {
						reward.writeNetData(buffer);
					}
				}

				for (QuestLink questLink : chapter.getQuestLinks()) {
					questLink.writeNetData(buffer);
				}
			}
		}

		FTBQuests.LOGGER.debug("Wrote " + (buffer.writerIndex() - pos) + " bytes, " + map.size() + " objects");
	}

	public final void readNetDataFull(FriendlyByteBuf buffer) {
		int pos = buffer.readerIndex();

		taskTypeIds.clear();
		rewardTypeIds.clear();

		for (TaskType type : TaskTypes.TYPES.values()) {
			type.intId = 0;
		}

		for (RewardType type : RewardTypes.TYPES.values()) {
			type.intId = 0;
		}

		int taskTypesSize = buffer.readVarInt();
		for (int i = 0; i < taskTypesSize; i++) {
			TaskType type = TaskTypes.TYPES.get(buffer.readResourceLocation());
			int id = buffer.readVarInt();

			if (type != null) {
				type.intId = id;
				taskTypeIds.put(type.intId, type);
			}
		}

		int rewardTypesSize = buffer.readVarInt();
		for (int i = 0; i < rewardTypesSize; i++) {
			RewardType type = RewardTypes.TYPES.get(buffer.readResourceLocation());
			int id = buffer.readVarInt();

			if (type != null) {
				type.intId = id;
				rewardTypeIds.put(type.intId, type);
			}
		}

		readNetData(buffer);

		rewardTables.clear();

		int rewardTableSize = buffer.readVarInt();
		for (int i = 0; i < rewardTableSize; i++) {
			RewardTable table = new RewardTable(this);
			table.id = buffer.readLong();
			rewardTables.add(table);
		}

		chapterGroups.clear();
		chapterGroups.add(defaultChapterGroup);

		int chapterGroupsSize = buffer.readVarInt();
		for (int i = 0; i < chapterGroupsSize; i++) {
			ChapterGroup group = new ChapterGroup(this);
			group.id = buffer.readLong();
			chapterGroups.add(group);
		}

		for (ChapterGroup group : chapterGroups) {
			int c = buffer.readVarInt();

			for (int i = 0; i < c; i++) {
				Chapter chapter = new Chapter(this, group);
				chapter.id = buffer.readLong();
				group.chapters.add(chapter);

				int questCount = buffer.readVarInt();
				for (int j = 0; j < questCount; j++) {
					Quest quest = new Quest(chapter);
					quest.id = buffer.readLong();
					chapter.getQuests().add(quest);

					int t = buffer.readVarInt();

					for (int k = 0; k < t; k++) {
						TaskType type = taskTypeIds.get(buffer.readVarInt());
						Task task = type.provider.create(quest);
						task.id = buffer.readLong();
						quest.tasks.add(task);
					}

					int r = buffer.readVarInt();

					for (int k = 0; k < r; k++) {
						RewardType type = rewardTypeIds.get(buffer.readVarInt());
						Reward reward = type.provider.create(quest);
						reward.id = buffer.readLong();
						quest.rewards.add(reward);
					}
				}

				int questLinkCount = buffer.readVarInt();
				for (int j = 0; j < questLinkCount; j++) {
					QuestLink questLink = new QuestLink(chapter, 0L);
					questLink.id = buffer.readLong();
					chapter.getQuestLinks().add(questLink);
				}
			}
		}

		refreshIDMap();

		for (RewardTable table : rewardTables) {
			table.readNetData(buffer);
		}

		for (ChapterGroup group : chapterGroups) {
			if (!group.isDefaultGroup()) {
				group.readNetData(buffer);
			}
		}

		for (ChapterGroup group : chapterGroups) {
			for (Chapter chapter : group.chapters) {
				chapter.readNetData(buffer);

				for (Quest quest : chapter.getQuests()) {
					quest.readNetData(buffer);

					for (Task task : quest.tasks) {
						task.readNetData(buffer);
					}

					for (Reward reward : quest.rewards) {
						reward.readNetData(buffer);
					}
				}

				for (QuestLink questLink : chapter.getQuestLinks()) {
					questLink.readNetData(buffer);
				}
			}
		}

		updateLootCrates();

		FTBQuests.LOGGER.info("Read " + (buffer.readerIndex() - pos) + " bytes, " + map.size() + " objects");
	}

	@Override
	public long getParentID() {
		return 0L;
	}

	@Nullable
	public TeamData getNullableTeamData(UUID id) {
		return teamDataMap.get(id);
	}

	public TeamData getData(UUID teamId) {
		TeamData teamData = teamDataMap.get(teamId);

		if (teamData == null) {
			teamData = new TeamData(teamId);
			teamData.file = this;
			teamDataMap.put(teamData.uuid, teamData);
		}

		return teamData;
	}

	public TeamData getData(TeamBase team) {
		return getData(Objects.requireNonNull(team, "Non-null team required!").getId());
	}

	public TeamData getData(Entity player) {
		return getData(FTBTeamsAPI.getPlayerTeamID(player.getUUID()));
	}

	public Collection<TeamData> getAllData() {
		return teamDataMap.values();
	}

	public abstract void deleteObject(long id);

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.file");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		return ThemeProperties.MODPACK_ICON.get(this);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addList("emergency_items", emergencyItems, new ItemStackConfig(false, false), ItemStack.EMPTY);
		config.addInt("emergency_items_cooldown", emergencyItemsCooldown, v -> emergencyItemsCooldown = v, 300, 0, Integer.MAX_VALUE);
		config.addBool("drop_loot_crates", dropLootCrates, v -> dropLootCrates = v, false);
		config.addBool("disable_gui", disableGui, v -> disableGui = v, false);
		config.addDouble("grid_scale", gridScale, v -> gridScale = v, 0.5D, 1D / 32D, 8D);
		config.addString("lock_message", lockMessage, v -> lockMessage = v, "");
		config.addEnum("progression_mode", progressionMode, v -> progressionMode = v, ProgressionMode.NAME_MAP_NO_DEFAULT);
		config.addInt("detection_delay", detectionDelay, v -> detectionDelay = v, 20, 0, 200);

		ConfigGroup defaultsGroup = config.getGroup("defaults");
		defaultsGroup.addBool("reward_team", defaultRewardTeam, v -> defaultRewardTeam = v, false);
		defaultsGroup.addBool("consume_items", defaultTeamConsumeItems, v -> defaultTeamConsumeItems = v, false);
		defaultsGroup.addEnum("autoclaim_rewards", defaultRewardAutoClaim, v -> defaultRewardAutoClaim = v, RewardAutoClaim.NAME_MAP_NO_DEFAULT);
		defaultsGroup.addEnum("quest_shape", defaultQuestShape, v -> defaultQuestShape = v, QuestShape.idMap);
		defaultsGroup.addBool("quest_disable_jei", defaultQuestDisableJEI, v -> defaultQuestDisableJEI = v, false);

		ConfigGroup d = config.getGroup("loot_crate_no_drop");
		d.addInt("passive", lootCrateNoDrop.passive, v -> lootCrateNoDrop.passive = v, 0, 0, Integer.MAX_VALUE).setNameKey("ftbquests.loot.entitytype.passive");
		d.addInt("monster", lootCrateNoDrop.monster, v -> lootCrateNoDrop.monster = v, 0, 0, Integer.MAX_VALUE).setNameKey("ftbquests.loot.entitytype.monster");
		d.addInt("boss", lootCrateNoDrop.boss, v -> lootCrateNoDrop.boss = v, 0, 0, Integer.MAX_VALUE).setNameKey("ftbquests.loot.entitytype.boss");
	}

	@Override
	public void clearCachedData() {
		super.clearCachedData();

		allTasks = null;
		submitTasks = null;
		craftingTasks = null;

		for (ChapterGroup group : chapterGroups) {
			group.clearCachedData();
		}

		clearCachedProgress();

		ClearFileCacheEvent.EVENT.invoker().accept(this);
	}

	public void clearCachedProgress() {
		for (TeamData data : getAllData()) {
			data.clearCachedProgress();
		}
	}

	public long newID() {
		return readID(0L);
	}

	public long readID(long id) {
		while (id == 0L || id == 1L || map.get(id) != null) {
			id = Math.abs(MathUtils.RAND.nextLong());
			save();
		}

		return id;
	}

	public long readID(@Nullable Tag tag) {
		if (tag instanceof NumericTag) {
			save();
			return readID(((NumericTag) tag).getAsLong());
		} else if (tag instanceof StringTag) {
			try {
				String id = tag.getAsString();
				return readID(Long.parseLong(id.charAt(0) == '#' ? id.substring(1) : id, 16));
			} catch (Exception ex) {
			}
		}

		return newID();
	}

	public long getID(@Nullable Object o) {
		if (o == null) {
			return 0L;
		} else if (o instanceof Number) {
			return ((Number) o).longValue();
		} else if (o instanceof NumericTag) {
			return ((NumericTag) o).getAsLong();
		} else if (o instanceof StringTag) {
			return getID(((StringTag) o).getAsString());
		}

		String id = o.toString();
		long idl = parseCodeString(id);

		if (idl == 0L && id.length() >= 2 && id.charAt(0) == '#') {
			String t = id.substring(1);

			for (QuestObjectBase b : map.values()) {
				if (b.hasTag(t)) {
					return b.id;
				}
			}
		}

		return idl;
	}

	@Nullable
	public LootCrate getRandomLootCrate(Entity entity, RandomSource random) {
		int totalWeight = lootCrateNoDrop.getWeight(entity);

		for (RewardTable table : rewardTables) {
			if (table.lootCrate != null) {
				totalWeight += table.lootCrate.drops.getWeight(entity);
			}
		}

		if (totalWeight <= 0) {
			return null;
		}

		int number = random.nextInt(totalWeight) + 1;
		int currentWeight = lootCrateNoDrop.getWeight(entity);

		if (currentWeight < number) {
			for (RewardTable table : rewardTables) {
				if (table.lootCrate != null) {
					currentWeight += table.lootCrate.drops.getWeight(entity);

					if (currentWeight >= number) {
						return table.lootCrate;
					}
				}
			}
		}

		return null;
	}

    @Override
	public Set<RecipeModHelper.Components> componentsToRefresh() {
		return EnumSet.allOf(RecipeModHelper.Components.class);
	}

	public final Collection<QuestObjectBase> getAllObjects() {
		return map.values();
	}

	@Override
	public boolean isVisible(TeamData data) {
		for (ChapterGroup group : chapterGroups) {
			if (group.isVisible(data)) {
				return true;
			}
		}

		return false;
	}

	public List<Chapter> getAllChapters() {
		List<Chapter> list = new ArrayList<>();

		for (ChapterGroup g : chapterGroups) {
			list.addAll(g.chapters);
		}

		return list;
	}

	public List<Task> getAllTasks() {
		if (allTasks == null) {
			allTasks = new ArrayList<>();

			for (ChapterGroup g : chapterGroups) {
				for (Chapter c : g.chapters) {
					for (Quest q : c.getQuests()) {
						allTasks.addAll(q.tasks);
					}
				}
			}
		}

		return allTasks;
	}

	public List<Task> getSubmitTasks() {
		if (submitTasks == null) {
			submitTasks = new ArrayList<>();

			for (Task task : getAllTasks()) {
				if (task.submitItemsOnInventoryChange()) {
					submitTasks.add(task);
				}
			}
		}

		return submitTasks;
	}

	public List<Task> getCraftingTasks() {
		if (craftingTasks == null) {
			craftingTasks = getAllTasks().stream().filter(task -> task instanceof ItemTask i && i.onlyFromCrafting.get(false)).toList();
		}
		return craftingTasks;
	}

	public List<Chapter> getVisibleChapters(TeamData data) {
		List<Chapter> list = new ArrayList<>();

		for (ChapterGroup group : chapterGroups) {
			list.addAll(group.getVisibleChapters(data));
		}

		return list;
	}

	@Nullable
	public Chapter getFirstVisibleChapter(TeamData data) {
		for (ChapterGroup group : chapterGroups) {
			Chapter c = group.getFirstVisibleChapter(data);

			if (c != null) {
				return c;
			}
		}

		return null;
	}

	public <T extends QuestObjectBase> List<T> collect(Predicate<QuestObjectBase> filter) {
		List<T> list = new ArrayList<>();

		for (QuestObjectBase base : getAllObjects()) {
			if (filter.test(base)) {
				list.add((T) base);
			}
		}

		if (list.isEmpty()) {
			return Collections.emptyList();
		} else if (list.size() == 1) {
			return Collections.singletonList(list.get(0));
		}

		return list;
	}

	public <T extends QuestObjectBase> List<T> collect(Class<T> clazz) {
		return collect(o -> clazz.isAssignableFrom(o.getClass()));
	}

	public String getDefaultQuestShape() {
		return defaultQuestShape;
	}

	public void addData(TeamData data, boolean override) {
		if (override || !teamDataMap.containsKey(data.uuid)) {
			teamDataMap.put(data.uuid, data);
		}
	}

	public void refreshGui() {
		clearCachedData();
	}

	@Override
	public Collection<? extends QuestObject> getChildren() {
		return chapterGroups;
	}

	@Override
	public boolean hasUnclaimedRewardsRaw(TeamData teamData, UUID player) {
		for (ChapterGroup group : chapterGroups) {
			if (teamData.hasUnclaimedRewards(player, group)) {
				return true;
			}
		}

		return false;
	}

	public ProgressionMode getProgressionMode() {
		return progressionMode;
	}

	public abstract boolean isPlayerOnTeam(Player player, TeamData teamData);
}
