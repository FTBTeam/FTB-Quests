package dev.ftb.mods.ftbquests.quest;

import dev.architectury.utils.Env;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ItemStackConfig;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.math.MathUtils;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.QuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.events.*;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.net.DeleteObjectResponseMessage;
import dev.ftb.mods.ftbquests.quest.loot.EntityWeight;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.*;
import dev.ftb.mods.ftbquests.quest.task.*;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import dev.ftb.mods.ftbquests.quest.translation.TranslationManager;
import dev.ftb.mods.ftbquests.util.FileUtils;
import dev.ftb.mods.ftbquests.util.TextUtils;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class BaseQuestFile extends QuestObject implements QuestFile {
	public static int VERSION = 13;

	public static final StreamCodec<RegistryFriendlyByteBuf,BaseQuestFile> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public BaseQuestFile decode(RegistryFriendlyByteBuf buf) {
			return Util.make(FTBQuestsClient.createClientQuestFile(), file -> file.readNetDataFull(buf));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, BaseQuestFile file) {
			file.writeNetDataFull(buf);
        }
    };

	private final DefaultChapterGroup defaultChapterGroup;
	final List<ChapterGroup> chapterGroups;
	private final List<RewardTable> rewardTables;
	protected final Map<UUID, TeamData> teamDataMap;

	private final Long2ObjectOpenHashMap<QuestObjectBase> questObjectMap;

	protected final Int2ObjectOpenHashMap<TaskType> taskTypeIds;
	protected final Int2ObjectOpenHashMap<RewardType> rewardTypeIds;

	private final TranslationManager translationManager;

	private final List<ItemStack> emergencyItems;
	private int emergencyItemsCooldown;
	private int fileVersion;

	private boolean defaultPerTeamReward;
	private boolean defaultTeamConsumeItems;
	private RewardAutoClaim defaultRewardAutoClaim;
	private String defaultQuestShape;
	private boolean defaultQuestDisableJEI;

	private boolean dropLootCrates;
	private final EntityWeight lootCrateNoDrop;
	private boolean disableGui;
	private double gridScale;
	private boolean pauseGame;
	protected String lockMessage;
	private ProgressionMode progressionMode;
	private int detectionDelay;

	private List<Task> allTasks;
	private List<Task> submitTasks;
	private List<Task> craftingTasks;

	public BaseQuestFile() {
		super(1L);

		fileVersion = 0;
		defaultChapterGroup = new DefaultChapterGroup(this);
		chapterGroups = new ArrayList<>();
		chapterGroups.add(defaultChapterGroup);
		rewardTables = new ArrayList<>();
		teamDataMap = new HashMap<>();

		questObjectMap = new Long2ObjectOpenHashMap<>();
		taskTypeIds = new Int2ObjectOpenHashMap<>();
		rewardTypeIds = new Int2ObjectOpenHashMap<>();

		emergencyItems = new ArrayList<>();
		emergencyItemsCooldown = 300;

		defaultPerTeamReward = false;
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

		translationManager = new TranslationManager();
	}

	public abstract Env getSide();

	public abstract HolderLookup.Provider holderLookup();

	public boolean isServerSide() {
		return getSide() == Env.SERVER;
	}

	@Override
	public BaseQuestFile getQuestFile() {
		return this;
	}

	@Override
	public QuestObjectType getObjectType() {
		return QuestObjectType.FILE;
	}

	public boolean isLoading() {
		return false;
	}

	@Override
	public boolean canEdit() {
		return false;
	}

	public Path getFolder() {
		throw new IllegalStateException("This quest file doesn't have a folder!");
	}

	public TranslationManager getTranslationManager() {
		return translationManager;
	}

	@Override
	public int getRelativeProgressFromChildren(TeamData data) {
		MutableInt progress = new MutableInt(0);
		MutableInt chapters = new MutableInt(0);

		forAllChapters(chapter -> {
			progress.add(data.getRelativeProgress(chapter));
			chapters.increment();
		});

		return getRelativeProgressFromChildren(progress.intValue(), chapters.intValue());
	}

	@Override
	public void onStarted(QuestProgressEventData<?> data) {
		data.setStarted(id);
		ObjectStartedEvent.FILE.invoker().act(new ObjectStartedEvent.FileEvent(data.withObject(this)));
	}

	@Override
	public void onCompleted(QuestProgressEventData<?> data) {
		data.setCompleted(id);
		ObjectCompletedEvent.FILE.invoker().act(new ObjectCompletedEvent.FileEvent(data.withObject(this)));

		if (!disableToast) {
			data.notifyPlayers(id);
		}
	}

	@Override
	public void deleteSelf() {
		invalid = true;
	}

	@Override
	public void deleteChildren() {
		forAllChapters(chapter -> {
			chapter.deleteChildren();
			chapter.invalid = true;
		});

		defaultChapterGroup.clearChapters();
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

		QuestObjectBase object = questObjectMap.get(id);
		return object == null || object.invalid ? null : object;
	}

	@Nullable
	public QuestObject get(long id) {
		return getBase(id) instanceof QuestObject qo ? qo : null;
	}

	@Nullable
	public QuestObjectBase remove(long id) {
		QuestObjectBase object = questObjectMap.remove(id);

		if (object != null) {
			if (object instanceof QuestObject qo) {
				forAllQuests(quest -> quest.removeDependency(qo));
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
				if (table.getLootCrate() != null && table.getLootCrate().getStringID().equals(id)) {
					return table.getLootCrate();
				}
			}
		}

		RewardTable table = getRewardTable(getID(id));
		return table == null ? null : table.getLootCrate();
	}

	public ChapterGroup getChapterGroup(long id) {
		QuestObjectBase object = getBase(id);
		return object instanceof ChapterGroup ? (ChapterGroup) object : defaultChapterGroup;
	}

	public void refreshIDMap() {
		clearCachedData();
		questObjectMap.clear();

		chapterGroups.forEach(group -> questObjectMap.put(group.id, group));
		rewardTables.forEach(table -> questObjectMap.put(table.id, table));

		forAllChapters(chapter -> {
			questObjectMap.put(chapter.id, chapter);

			for (Quest quest : chapter.getQuests()) {
				questObjectMap.put(quest.id, quest);
				quest.getTasks().forEach(task -> questObjectMap.put(task.id, task));
				quest.getRewards().forEach(reward -> questObjectMap.put(reward.id, reward));
			}

			chapter.getQuestLinks().forEach(link -> questObjectMap.put(link.id, link));
		});

		clearCachedData();
	}

	public QuestObjectBase create(long id, QuestObjectType type, long parent, CompoundTag extra) {
		switch (type) {
			case CHAPTER -> {
				return new Chapter(id, this, getChapterGroup(extra.getLong("group")));
			}
			case QUEST -> {
				Chapter chapter = getChapter(parent);
				if (chapter != null) {
					return new Quest(id, chapter);
				}
				throw new IllegalArgumentException("Parent chapter not found!");
			}
			case QUEST_LINK -> {
				Chapter chapter = getChapter(parent);
				if (chapter != null) {
					return new QuestLink(id, chapter, 0L);
				}
				throw new IllegalArgumentException("Parent chapter not found!");
			}
			case TASK -> {
				Quest quest = getQuest(parent);
				if (quest != null) {
					Task task = TaskType.createTask(id, quest, extra.getString("type"));
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
					Reward reward = RewardType.createReward(id, quest, extra.getString("type"));
					if (reward != null) {
						return reward;
					}
					throw new IllegalArgumentException("Unknown reward type!");
				}
				throw new IllegalArgumentException("Parent quest not found!");
			}
			case REWARD_TABLE -> {
				return new RewardTable(id, this);
			}
			case CHAPTER_GROUP -> {
				return new ChapterGroup(id, this);
			}
			default -> throw new IllegalArgumentException("Unknown type: " + type);
		}
	}

	@Override
	public final void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);
		nbt.putBoolean("default_reward_team", defaultPerTeamReward);
		nbt.putBoolean("default_consume_items", defaultTeamConsumeItems);
		nbt.putString("default_autoclaim_rewards", defaultRewardAutoClaim.id);
		nbt.putString("default_quest_shape", defaultQuestShape);
		nbt.putBoolean("default_quest_disable_jei", defaultQuestDisableJEI);

		if (!emergencyItems.isEmpty()) {
			nbt.put("emergency_items", Util.make(new ListTag(), l -> {
				for (ItemStack stack : emergencyItems) {
					l.add(stack.save(provider));
				}
			}));
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
	public final void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		defaultPerTeamReward = nbt.getBoolean("default_reward_team");
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
			emergencyItems.add(itemOrMissingFromNBT(emergencyItemsTag.getCompound(i), provider));
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

	public final void writeDataFull(Path folder, HolderLookup.Provider provider) {
		boolean prev = false;
		try {
			// Sorting keys ensure consistent sort order in the saved quest file
			// Since questbook data is commonly stored under version control, this minimizes extraneous
			//  version control changes stemming from unpredictable hashmap key ordering
			prev = SNBT.setShouldSortKeysOnWrite(true);

			SNBTCompoundTag fileNBT = new SNBTCompoundTag();
			fileNBT.putInt("version", VERSION);
			writeData(fileNBT, provider);
			SNBT.write(folder.resolve("data.snbt"), fileNBT);

			for (ChapterGroup group : chapterGroups) {
				for (int ci = 0; ci < group.getChapters().size(); ci++) {
					Chapter chapter = group.getChapters().get(ci);
					SNBTCompoundTag chapterNBT = new SNBTCompoundTag();
					chapterNBT.putString("id", chapter.getCodeString());
					chapterNBT.putString("group", group.isDefaultGroup() ? "" : group.getCodeString());
					chapterNBT.putInt("order_index", ci);
					chapter.writeData(chapterNBT, provider);

					ListTag questList = new ListTag();
					for (Quest quest : chapter.getQuests()) {
						if (quest.isValid()) {
							SNBTCompoundTag questNBT = new SNBTCompoundTag();
							quest.writeData(questNBT, provider);
							questNBT.putString("id", quest.getCodeString());
							if (!quest.getTasks().isEmpty()) {
								quest.writeTasks(questNBT, provider);
							}
							if (!quest.getRewards().isEmpty()) {
								quest.writeRewards(questNBT, provider);
							}
							questList.add(questNBT);
						}
					}
					chapterNBT.put("quests", questList);

					ListTag linkList = new ListTag();
					for (QuestLink link : chapter.getQuestLinks()) {
						if (link.getQuest().isPresent()) {
							SNBTCompoundTag linkNBT = new SNBTCompoundTag();
							link.writeData(linkNBT, provider);
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
				table.writeData(tableNBT, provider);
				SNBT.write(folder.resolve("reward_tables/" + table.getFilename() + ".snbt"), tableNBT);
			}

			ListTag chapterGroupTag = new ListTag();

			for (ChapterGroup group : chapterGroups) {
				if (!group.isDefaultGroup()) {
					SNBTCompoundTag groupTag = new SNBTCompoundTag();
					groupTag.singleLine();
					groupTag.putString("id", group.getCodeString());
					group.writeData(groupTag, provider);
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

	public final void readDataFull(Path folder, HolderLookup.Provider provider) {
		clearCachedData();
		questObjectMap.clear();
		defaultChapterGroup.clearChapters();
		chapterGroups.clear();
		chapterGroups.add(defaultChapterGroup);
		rewardTables.clear();

		MutableInt chapterCounter = new MutableInt();
		MutableInt questCounter = new MutableInt();

		final Long2ObjectOpenHashMap<CompoundTag> dataCache = new Long2ObjectOpenHashMap<>();
		CompoundTag fileNBT = SNBT.read(folder.resolve("data.snbt"));

		if (fileNBT != null) {
			fileVersion = fileNBT.getInt("version");
			questObjectMap.put(1, this);
			readData(fileNBT, provider);
			handleLegacyFileNBT(fileNBT);
		}

		translationManager.loadFromNBT(folder.resolve("lang"));

		Path groupsFile = folder.resolve("chapter_groups.snbt");
		if (Files.exists(groupsFile)) {
			CompoundTag chapterGroupsTag = SNBT.read(groupsFile);

			if (chapterGroupsTag != null) {
				ListTag groupListTag = chapterGroupsTag.getList("chapter_groups", Tag.TAG_COMPOUND);

				for (int i = 0; i < groupListTag.size(); i++) {
					CompoundTag groupNBT = groupListTag.getCompound(i);
					ChapterGroup chapterGroup = new ChapterGroup(readID(groupNBT.get("id")), this);

					handleLegacyChapterGroupNBT(groupNBT, chapterGroup);

					questObjectMap.put(chapterGroup.id, chapterGroup);
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
						Chapter chapter = new Chapter(readID(chapterNBT.get("id")),this,
								getChapterGroup(getID(chapterNBT.get("group"))),
								path.getFileName().toString().replace(".snbt", "")
						);

						handleLegacyChapterNBT(chapterNBT, chapter);

						objectOrderMap.put(chapter.id, chapterNBT.getInt("order_index"));
						questObjectMap.put(chapter.id, chapter);
						dataCache.put(chapter.id, chapterNBT);
						chapter.getGroup().addChapter(chapter);

						ListTag questList = chapterNBT.getList("quests", Tag.TAG_COMPOUND);

						for (int i = 0; i < questList.size(); i++) {
							CompoundTag questNBT = questList.getCompound(i);
							Quest quest = new Quest(readID(questNBT.get("id")), chapter);

							handleLegacyQuestNBT(quest, questNBT);

							questObjectMap.put(quest.id, quest);
							dataCache.put(quest.id, questNBT);
							chapter.addQuest(quest);

							ListTag taskList = questNBT.getList("tasks", Tag.TAG_COMPOUND);

							for (int j = 0; j < taskList.size(); j++) {
								CompoundTag taskNBT = taskList.getCompound(j);
								long taskId = readID(taskNBT.get("id"));
								Task task = TaskType.createTask(taskId, quest, taskNBT.getString("type"));

								handleLegacyTaskNBT(task, taskNBT);

								if (task == null) {
									task = new CustomTask(taskId, quest);
									task.setRawTitle("Unknown type: " + taskNBT.getString("type"));
								}

								questObjectMap.put(task.id, task);
								dataCache.put(task.id, taskNBT);
								quest.addTask(task);
							}

							ListTag rewardList = questNBT.getList("rewards", Tag.TAG_COMPOUND);

							for (int j = 0; j < rewardList.size(); j++) {
								CompoundTag rewardNBT = rewardList.getCompound(j);
								long rewardId = readID(rewardNBT.get("id"));
								Reward reward = RewardType.createReward(rewardId, quest, rewardNBT.getString("type"));
								if (reward == null) {
									reward = new CustomReward(rewardId, quest);
									reward.setRawTitle("Unknown type: " + rewardNBT.getString("type"));
								}

								questObjectMap.put(reward.id, reward);
								dataCache.put(reward.id, rewardNBT);
								quest.addReward(reward);
							}

							questCounter.increment();
						}

						ListTag questLinks = chapterNBT.getList("quest_links", Tag.TAG_COMPOUND);
						for (int i = 0; i < questLinks.size(); i++) {
							CompoundTag linkNBT = questLinks.getCompound(i);
							QuestLink link = new QuestLink(readID(linkNBT.get("id")), chapter, readID(linkNBT.get("linked_quest")));
							chapter.addQuestLink(link);
							questObjectMap.put(link.id, link);
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
						String filename = path.getFileName().toString().replace(".snbt", "");
						RewardTable table = new RewardTable(readID(tableNBT.get("id")), this, filename);
						objectOrderMap.put(table.id, tableNBT.getInt("order_index"));
						questObjectMap.put(table.id, table);
						dataCache.put(table.id, tableNBT);
						rewardTables.add(table);
					}
				});
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		for (QuestObjectBase object : questObjectMap.values()) {
			CompoundTag data = dataCache.get(object.id);

			if (data != null) {
				object.readData(data, provider);
			}
		}

		for (ChapterGroup group : chapterGroups) {
			group.sortChapters(Comparator.comparingInt(c -> objectOrderMap.get(c.id)));

			for (Chapter chapter : group.getChapters()) {
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
			markDirty();
		}

        FTBQuests.LOGGER.info("Loaded {} chapter groups, {} chapters, {} quests, {} reward tables", chapterGroups.size(), chapterCounter, questCounter, rewardTables.size());
	}

	private void handleLegacyFileNBT(CompoundTag fileNBT) {
		if (fileNBT.contains("title", Tag.TAG_STRING)) {
			translationManager.addTranslation(this, "en_us", TranslationKey.TITLE, fileNBT.getString("title"));
			markDirty();
		}
	}

	private void handleLegacyChapterGroupNBT(CompoundTag groupNBT, ChapterGroup chapterGroup) {
		if (groupNBT.contains("title", Tag.TAG_STRING)) {
			translationManager.addTranslation(chapterGroup, "en_us", TranslationKey.TITLE, groupNBT.getString("title"));
			markDirty();
		}
	}

	private void handleLegacyChapterNBT(CompoundTag chapterNBT, Chapter chapter) {
		if (chapterNBT.contains("title", Tag.TAG_STRING)) {
			translationManager.addTranslation(chapter, "en_us", TranslationKey.TITLE, chapterNBT.getString("title"));
			markDirty();
		}
		if (chapterNBT.contains("subtitle", Tag.TAG_LIST)) {
			translationManager.addTranslation(chapter, "en_us", TranslationKey.CHAPTER_SUBTITLE, TextUtils.fromListTag(chapterNBT.getList("subtitle", Tag.TAG_STRING)));
			markDirty();
		}
	}

	private void handleLegacyQuestNBT(Quest quest, CompoundTag questNBT) {
		if (questNBT.contains("title", Tag.TAG_STRING)) {
			translationManager.addTranslation(quest, "en_us", TranslationKey.TITLE, questNBT.getString("title"));
			markDirty();
		}
		if (questNBT.contains("subtitle", Tag.TAG_STRING)) {
			translationManager.addTranslation(quest, "en_us", TranslationKey.QUEST_SUBTITLE, questNBT.getString("subtitle"));
			markDirty();
		}
		if (questNBT.contains("description", Tag.TAG_LIST)) {
			translationManager.addTranslation(quest, "en_us", TranslationKey.QUEST_DESC, TextUtils.fromListTag(questNBT.getList("description", Tag.TAG_STRING)));
			markDirty();
		}
	}

	private void handleLegacyTaskNBT(Task task, CompoundTag taskNBT) {
		if (taskNBT.contains("title", Tag.TAG_STRING)) {
			translationManager.addTranslation(task, "en_us", TranslationKey.TITLE, taskNBT.getString("title"));
			markDirty();
		}
	}

	public void updateLootCrates() {
		Set<String> prevCrateNames = new HashSet<>(LootCrate.LOOT_CRATES.keySet());
		Collection<ItemStack> oldStacks = LootCrate.allCrateStacks();

		LootCrate.LOOT_CRATES.clear();
		for (RewardTable table : rewardTables) {
			if (table.getLootCrate() != null) {
				LootCrate.LOOT_CRATES.put(table.getLootCrate().getStringID(), table.getLootCrate());
			}
		}

		if (!isServerSide() && !prevCrateNames.equals(LootCrate.LOOT_CRATES.keySet())) {
			FTBQuestsClient.rebuildCreativeTabs();
			FTBQuests.getRecipeModHelper().updateItemsDynamic(oldStacks, LootCrate.allCrateStacks());
		}

		FTBQuests.LOGGER.debug("Updated loot crates (was {}, now {})", prevCrateNames.size(), LootCrate.LOOT_CRATES.size());
	}

	public void markDirty() {
	}

	@Override
	public final void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, emergencyItems);
		buffer.writeVarInt(emergencyItemsCooldown);
		buffer.writeBoolean(defaultPerTeamReward);
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
	public final void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);

		emergencyItems.clear();
		emergencyItems.addAll(ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer));
		emergencyItemsCooldown = buffer.readVarInt();
		defaultPerTeamReward = buffer.readBoolean();
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

	public final void writeNetDataFull(RegistryFriendlyByteBuf buffer) {
		int pos = buffer.writerIndex();

		buffer.writeVarInt(TaskTypes.TYPES.size());
		for (TaskType type : TaskTypes.TYPES.values()) {
			buffer.writeResourceLocation(type.getTypeId());
			buffer.writeVarInt(type.internalId);
		}

		buffer.writeVarInt(RewardTypes.TYPES.size());
		for (RewardType type : RewardTypes.TYPES.values()) {
			buffer.writeResourceLocation(type.getTypeId());
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
			buffer.writeVarInt(group.getChapters().size());

			for (Chapter chapter : group.getChapters()) {
				buffer.writeLong(chapter.id);
				buffer.writeVarInt(chapter.getQuests().size());

				for (Quest quest : chapter.getQuests()) {
					buffer.writeLong(quest.id);

					buffer.writeVarInt(quest.getTasks().size());
					quest.getTasks().forEach(task -> {
						buffer.writeVarInt(task.getType().internalId);
						buffer.writeLong(task.id);
					});

					buffer.writeVarInt(quest.getRewards().size());
					quest.getRewards().forEach(reward -> {
						buffer.writeVarInt(reward.getType().intId);
						buffer.writeLong(reward.id);
					});
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
			for (Chapter chapter : group.getChapters()) {
				chapter.writeNetData(buffer);

				chapter.getQuests().forEach(quest -> {
					quest.writeNetData(buffer);
					quest.getTasks().forEach(task -> task.writeNetData(buffer));
					quest.getRewards().forEach(reward -> reward.writeNetData(buffer));
				});

				chapter.getQuestLinks().forEach(questLink -> questLink.writeNetData(buffer));
			}
		}

        FTBQuests.LOGGER.debug("Wrote {} bytes, {} objects", buffer.writerIndex() - pos, questObjectMap.size());
	}

	public final void readNetDataFull(RegistryFriendlyByteBuf buffer) {
		int pos = buffer.readerIndex();

		taskTypeIds.clear();
		rewardTypeIds.clear();

		for (TaskType type : TaskTypes.TYPES.values()) {
			type.internalId = 0;
		}

		for (RewardType type : RewardTypes.TYPES.values()) {
			type.intId = 0;
		}

		int taskTypesSize = buffer.readVarInt();
		for (int i = 0; i < taskTypesSize; i++) {
			TaskType type = TaskTypes.TYPES.get(buffer.readResourceLocation());
			int id = buffer.readVarInt();

			if (type != null) {
				type.internalId = id;
				taskTypeIds.put(type.internalId, type);
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
			RewardTable table = new RewardTable(buffer.readLong(), this);
			rewardTables.add(table);
		}

		chapterGroups.clear();
		chapterGroups.add(defaultChapterGroup);

		int chapterGroupsSize = buffer.readVarInt();
		for (int i = 0; i < chapterGroupsSize; i++) {
			ChapterGroup group = new ChapterGroup(buffer.readLong(), this);
			chapterGroups.add(group);
		}

		for (ChapterGroup group : chapterGroups) {
			int chapterCount = buffer.readVarInt();
			for (int i = 0; i < chapterCount; i++) {
				Chapter chapter = new Chapter(buffer.readLong(), this, group);
				group.addChapter(chapter);

				int questCount = buffer.readVarInt();
				for (int j = 0; j < questCount; j++) {
					Quest quest = new Quest(buffer.readLong(), chapter);
					chapter.addQuest(quest);

					int taskCount = buffer.readVarInt();
					for (int k = 0; k < taskCount; k++) {
						TaskType type = taskTypeIds.get(buffer.readVarInt());
						quest.addTask(type.createTask(buffer.readLong(), quest));
					}

					int rewardCount = buffer.readVarInt();
					for (int k = 0; k < rewardCount; k++) {
						RewardType type = rewardTypeIds.get(buffer.readVarInt());
						quest.addReward(type.createReward(buffer.readLong(), quest));
					}
				}

				int questLinkCount = buffer.readVarInt();
				for (int j = 0; j < questLinkCount; j++) {
					QuestLink questLink = new QuestLink(buffer.readLong(), chapter, 0L);
					chapter.addQuestLink(questLink);
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
			for (Chapter chapter : group.getChapters()) {
				chapter.readNetData(buffer);

				for (Quest quest : chapter.getQuests()) {
					quest.readNetData(buffer);
					quest.getTasks().forEach(task -> task.readNetData(buffer));
					quest.getRewards().forEach(reward -> reward.readNetData(buffer));
				}

				for (QuestLink questLink : chapter.getQuestLinks()) {
					questLink.readNetData(buffer);
				}
			}
		}

        FTBQuests.LOGGER.info("Read {} bytes, {} objects", buffer.readerIndex() - pos, questObjectMap.size());
	}

	@Override
	public long getParentID() {
		return 0L;
	}

	@Override
	@Nullable
	public TeamData getNullableTeamData(UUID id) {
		return teamDataMap.get(id);
	}

	@Override
	public TeamData getOrCreateTeamData(UUID teamId) {
		return teamDataMap.computeIfAbsent(teamId, k -> new TeamData(teamId, this));
	}

	@Override
	public TeamData getOrCreateTeamData(Team team) {
		return getOrCreateTeamData(Objects.requireNonNull(team, "Non-null team required!").getId());
	}

	@Override
	public TeamData getOrCreateTeamData(Entity player) {
		return FTBTeamsAPI.api().getManager().getTeamForPlayerID(player.getUUID())
				.map(this::getOrCreateTeamData)
				.orElse(null);
	}

	@Override
	public Collection<TeamData> getAllTeamData() {
		return Collections.unmodifiableCollection(teamDataMap.values());
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
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addList("emergency_items", emergencyItems, new ItemStackConfig(false, false), ItemStack.EMPTY);
		config.addInt("emergency_items_cooldown", emergencyItemsCooldown, v -> emergencyItemsCooldown = v, 300, 0, Integer.MAX_VALUE);
		config.addBool("drop_loot_crates", dropLootCrates, v -> dropLootCrates = v, false);
		config.addBool("disable_gui", disableGui, v -> disableGui = v, false);
		config.addDouble("grid_scale", gridScale, v -> gridScale = v, 0.5D, 1D / 32D, 8D);
		config.addString("lock_message", lockMessage, v -> lockMessage = v, "");
		config.addEnum("progression_mode", progressionMode, v -> progressionMode = v, ProgressionMode.NAME_MAP_NO_DEFAULT);
		config.addInt("detection_delay", detectionDelay, v -> detectionDelay = v, 20, 0, 200);
		config.addBool("pause_game", pauseGame, v -> pauseGame = v, false);

		ConfigGroup defaultsGroup = config.getOrCreateSubgroup("defaults");
		defaultsGroup.addBool("reward_team", defaultPerTeamReward, v -> defaultPerTeamReward = v, false);
		defaultsGroup.addBool("consume_items", defaultTeamConsumeItems, v -> defaultTeamConsumeItems = v, false);
		defaultsGroup.addEnum("autoclaim_rewards", defaultRewardAutoClaim, v -> defaultRewardAutoClaim = v, RewardAutoClaim.NAME_MAP_NO_DEFAULT);
		defaultsGroup.addEnum("quest_shape", defaultQuestShape, v -> defaultQuestShape = v, QuestShape.idMap);
		defaultsGroup.addBool("quest_disable_jei", defaultQuestDisableJEI, v -> defaultQuestDisableJEI = v, false);

		ConfigGroup d = config.getOrCreateSubgroup("loot_crate_no_drop");
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
		getAllTeamData().forEach(TeamData::clearCachedProgress);
	}

	public long newID() {
		return readID(0L);
	}

	public long readID(long id) {
		while (id == 0L || id == 1L || questObjectMap.get(id) != null) {
			id = Math.abs(MathUtils.RAND.nextLong());
			markDirty();
		}

		return id;
	}

	public long readID(@Nullable Tag tag) {
		if (tag instanceof NumericTag) {
			markDirty();
			return readID(((NumericTag) tag).getAsLong());
		} else if (tag instanceof StringTag) {
			try {
				String id = tag.getAsString();
				return readID(Long.parseLong(id.charAt(0) == '#' ? id.substring(1) : id, 16));
			} catch (Exception ignored) {
			}
		}

		return newID();
	}

	public long getID(@Nullable Object obj) {
        switch (obj) {
            case null -> {
                return 0L;
            }
            case Number n -> {
                return n.longValue();
            }
            case NumericTag nt -> {
                return nt.getAsLong();
            }
            case StringTag st -> {
                return getID(st.getAsString());
            }
            default -> {
            }
        }

		String idStr = obj.toString();
		long id = parseCodeString(idStr);
		if (id == 0L && idStr.length() >= 2 && idStr.charAt(0) == '#') {
			String tagVal = idStr.substring(1);
			return questObjectMap.values().stream()
					.filter(qob -> qob.hasTag(tagVal))
					.findFirst()
					.map(qob -> qob.id)
					.orElse(id);
		}

		return id;
	}

	public Optional<LootCrate> makeRandomLootCrate(Entity entity, RandomSource random) {
		int totalWeight = lootCrateNoDrop.getWeight(entity);

		for (RewardTable table : rewardTables) {
			if (table.getLootCrate() != null) {
				totalWeight += table.getLootCrate().getDrops().getWeight(entity);
			}
		}

		if (totalWeight <= 0) {
			return Optional.empty();
		}

		int number = random.nextInt(totalWeight) + 1;
		int currentWeight = lootCrateNoDrop.getWeight(entity);

		if (currentWeight < number) {
			for (RewardTable table : rewardTables) {
				if (table.getLootCrate() != null) {
					currentWeight += table.getLootCrate().getDrops().getWeight(entity);
					if (currentWeight >= number) {
						return Optional.ofNullable(table.getLootCrate());
					}
				}
			}
		}

		return Optional.empty();
	}

	@Override
	public Set<RecipeModHelper.Components> componentsToRefresh() {
		return EnumSet.allOf(RecipeModHelper.Components.class);
	}

	public final Collection<QuestObjectBase> getAllObjects() {
		return Collections.unmodifiableCollection(questObjectMap.values());
	}

	@Override
	public boolean isVisible(TeamData data) {
		return chapterGroups.stream().anyMatch(group -> group.isVisible(data));
	}

	public List<Chapter> getAllChapters() {
		List<Chapter> list = new ArrayList<>();

		for (ChapterGroup g : chapterGroups) {
			list.addAll(g.getChapters());
		}

		return list;
	}

	public List<Task> getAllTasks() {
		if (allTasks == null) {
			allTasks = new ArrayList<>();
			forAllQuests(q -> allTasks.addAll(q.getTasks()));
		}
		return allTasks;
	}

	public List<Task> getSubmitTasks() {
		if (submitTasks == null) {
			submitTasks = getAllTasks().stream().filter(Task::submitItemsOnInventoryChange).toList();
		}
		return submitTasks;
	}

	public List<Task> getCraftingTasks() {
		if (craftingTasks == null) {
			craftingTasks = getAllTasks().stream().filter(task -> task instanceof ItemTask i && i.isOnlyFromCrafting()).toList();
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
		if (override || !teamDataMap.containsKey(data.getTeamId())) {
			teamDataMap.put(data.getTeamId(), data);
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

	public int getDetectionDelay() {
		return detectionDelay;
	}

	public boolean isPauseGame() {
		return pauseGame;
	}

	public boolean isDisableGui() {
		return disableGui;
	}

	public double getGridScale() {
		return gridScale;
	}

	public boolean isDropLootCrates() {
		return dropLootCrates;
	}

	public boolean isDefaultPerTeamReward() {
		return defaultPerTeamReward;
	}

	public boolean isDefaultTeamConsumeItems() {
		return defaultTeamConsumeItems;
	}

	public RewardAutoClaim getDefaultRewardAutoClaim() {
		return defaultRewardAutoClaim;
	}

	public List<ItemStack> getEmergencyItems() {
		return Collections.unmodifiableList(emergencyItems);
	}

	public int getEmergencyItemsCooldown() {
		return emergencyItemsCooldown;
	}

	public boolean isDefaultQuestDisableJEI() {
		return defaultQuestDisableJEI;
	}

	public abstract boolean isPlayerOnTeam(Player player, TeamData teamData);

	public TaskType getTaskType(int typeId) {
		return taskTypeIds.get(typeId);
	}

	public RewardType getRewardType(int typeId) {
		return rewardTypeIds.get(typeId);
	}

	public DefaultChapterGroup getDefaultChapterGroup() {
		return defaultChapterGroup;
	}

	public List<RewardTable> getRewardTables() {
		return Collections.unmodifiableList(rewardTables);
	}

	public void addRewardTable(RewardTable rewardTable) {
		rewardTables.add(rewardTable);
	}

	public void removeRewardTable(RewardTable rewardTable) {
		rewardTables.remove(rewardTable);
	}

	public int removeEmptyRewardTables(CommandSourceStack source) {
		MutableInt del = new MutableInt(0);

		for (RewardTable table : rewardTables) {
			if (table.getWeightedRewards().isEmpty()) {
				del.increment();
				table.invalid = true;
				FileUtils.delete(ServerQuestFile.INSTANCE.getFolder().resolve(table.getPath().orElseThrow()).toFile());
				NetworkHelper.sendToAll(source.getServer(), new DeleteObjectResponseMessage(table.id));
			}
		}

		if (rewardTables.removeIf(rewardTable -> rewardTable.invalid)) {
			refreshIDMap();
			markDirty();
		}

		return del.intValue();
	}

//	public String generateRewardTableName(String basename) {
//		String s = titleToID(basename).orElse(toString());
//		String filename = s;
//
//		Set<String> existingNames = rewardTables.stream().map(RewardTable::getFilename).collect(Collectors.toSet());
//		int i = 2;
//
//		while (existingNames.contains(filename)) {
//			filename = s + "_" + i;
//			i++;
//		}
//
//		return filename;
//	}

	public List<ChapterGroup> getChapterGroups() {
		return Collections.unmodifiableList(chapterGroups);
	}

	public void forAllChapterGroups(Consumer<ChapterGroup> consumer) {
		chapterGroups.forEach(consumer);
	}

	@Override
	public void forAllChapters(Consumer<Chapter> consumer) {
		forAllChapterGroups(g -> g.getChapters().forEach(consumer));
	}

	@Override
	public void forAllQuests(Consumer<Quest> consumer) {
		forAllChapters(c -> c.getQuests().forEach(consumer));
	}

	@Override
	public void forAllQuestLinks(Consumer<QuestLink> consumer) {
		forAllChapters(c -> c.getQuestLinks().forEach(consumer));
	}

	public boolean moveChapterGroup(long id, boolean movingUp) {
		ChapterGroup group = getChapterGroup(id);

		if (!group.isDefaultGroup()) {
			int index = chapterGroups.indexOf(group);
			if (index != -1 && movingUp ? (index > 1) : (index < chapterGroups.size() - 1)) {
				chapterGroups.remove(index);
				chapterGroups.add(movingUp ? index - 1 : index + 1, group);
				return true;
			}
		}
		return false;
	}

	public EntityWeight getLootCrateNoDrop() {
		return lootCrateNoDrop;
	}

	public abstract String getLocale();
}
