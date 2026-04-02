package dev.ftb.mods.ftbquests.quest;

import com.mojang.logging.LogUtils;
import de.marhali.json5.Json5Array;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import de.marhali.json5.Json5Primitive;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableItemStack;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.math.MathUtils;
import dev.ftb.mods.ftblibrary.platform.Env;
import dev.ftb.mods.ftblibrary.platform.event.NativeEventPosting;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.QuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.config.EditableLocaleConfig;
import dev.ftb.mods.ftbquests.api.event.ClearFileCacheEvent;
import dev.ftb.mods.ftbquests.api.event.CustomTaskEvent;
import dev.ftb.mods.ftbquests.api.event.progress.FileProgressEvent;
import dev.ftb.mods.ftbquests.api.event.progress.ProgressEventData;
import dev.ftb.mods.ftbquests.api.event.progress.ProgressType;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.net.DeleteObjectResponseMessage;
import dev.ftb.mods.ftbquests.quest.loot.EntityWeight;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.*;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.quest.translation.TranslationManager;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.client.ClientTeamManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class BaseQuestFile extends QuestObject implements QuestFile {
	public static final String FILE_SUFFIX = ".json5";
	public static int VERSION = 13;
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final StreamCodec<RegistryFriendlyByteBuf,BaseQuestFile> STREAM_CODEC = StreamCodec.of(
			(buf, file) -> file.writeNetDataFull(buf),
			buf -> Util.make(FTBQuestsClient.createClientQuestFile(), file -> file.readNetDataFull(buf))
	);

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
	private boolean hideExcludedQuests;
	private boolean dropLootCrates;
	private EntityWeight lootCrateNoDrop;
	private boolean disableGui;
	private double gridScale;
	private boolean pauseGame;
	protected String lockMessage;
	private ProgressionMode progressionMode;
	private int detectionDelay;
	private boolean showLockIcons;
	private boolean dropBookOnDeath;
	private String fallbackLocale;
	private boolean verifyOnLoad;

	@Nullable
	private List<Task> allTasks;
	@Nullable
	private List<Task> submitTasks;
	@Nullable
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
		lootCrateNoDrop = new EntityWeight(4000, 600, 0);
		disableGui = false;
		gridScale = 0.5D;
		pauseGame = false;
		lockMessage = "";
		progressionMode = ProgressionMode.LINEAR;
		detectionDelay = 20;
		dropBookOnDeath = false;
		hideExcludedQuests = false;
		verifyOnLoad = false;

		allTasks = null;

		translationManager = new TranslationManager();
		fallbackLocale = TranslationManager.DEFAULT_FALLBACK_LOCALE;
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
	public void onStarted(ProgressEventData<?> eventData) {
		eventData.setStarted(id);
		NativeEventPosting.get().postEvent(new FileProgressEvent.Data(ProgressType.STARTED, eventData.withObject(this)));
	}

	@Override
	public void onCompleted(ProgressEventData<?> eventData) {
		eventData.setCompleted(id);
		NativeEventPosting.get().postEvent(new FileProgressEvent.Data(ProgressType.COMPLETED, eventData.withObject(this)));

		if (!disableToast) {
			eventData.notifyPlayers(id);
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
		//noinspection ConstantValue
		return object == null || object.invalid ? null : object;
	}

	@Nullable
	public QuestObject get(long id) {
		return getBase(id) instanceof QuestObject qo ? qo : null;
	}

	@Nullable
	public QuestObjectBase remove(long id) {
		QuestObjectBase object = questObjectMap.remove(id);

		//noinspection ConstantValue
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

	public <T extends QuestObjectBase> T getQuestObjectOrThrow(long id, Class<T> cls) {
		QuestObjectBase object = getBase(id);
		if (object == null) {
			throw new IllegalArgumentException("Unknown object id " + id);
		} else if (cls.isAssignableFrom(object.getClass())) {
			return cls.cast(object);
		} else {
			throw new IllegalArgumentException("Wrong class for object id " + id + ": wanted " + cls.getName() + ", got " + object.getClass().getName());
		}
	}

	@Nullable
	public Chapter getChapter(long id) {
		QuestObjectBase object = getBase(id);
		return object instanceof Chapter ? (Chapter) object : null;
	}

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
			chapter.getImages().forEach(img -> questObjectMap.put(img.id, img));
		});

		refreshRewardTableRewardIDs();
	}

	public void refreshRewardTableRewardIDs() {
		rewardTables.forEach(table -> table.getWeightedRewards().forEach(wr -> questObjectMap.put(wr.getReward().id, wr.getReward())));
	}

	public QuestObjectBase create(long id, QuestObjectType type, long parent, Json5Object extraData) {
		switch (type) {
			case CHAPTER -> {
				return new Chapter(id, this, getChapterGroup(Json5Util.getLong(extraData, "group").orElse(0L)));
			}
			case QUEST -> {
				return new Quest(id, getChapterOrThrow(parent));
			}
			case QUEST_LINK -> {
				return new QuestLink(id, getChapterOrThrow(parent), 0L);
			}
			case TASK -> {
				Quest quest = getQuest(parent);
				if (quest != null) {
					return TaskType.createTask(id, quest, Json5Util.getString(extraData, "type").orElse(""));
				}
				throw new IllegalArgumentException("Parent quest not found!");
			}
			case REWARD -> {
				String rewardType = Json5Util.getString(extraData, "type").orElse("");
				if (RewardTable.isFakeQuestId(parent)) {
					return RewardTable.createRewardForTable(id, rewardType, this);
				} else {
					Quest quest = getQuestObjectOrThrow(parent, Quest.class);
					return RewardType.createReward(id, quest, rewardType);
				}
			}
			case REWARD_TABLE -> {
				return new RewardTable(id, this);
			}
			case CHAPTER_GROUP -> {
				return new ChapterGroup(id, this);
			}
			case IMAGE -> {
				return new ChapterImage(id, getChapterOrThrow(parent));
			}
			default -> throw new IllegalArgumentException("Unknown type: " + type);
		}
	}

	@Override
	public final void writeData(Json5Object json, HolderLookup.Provider provider) {
		super.writeData(json, provider);
		json.addProperty("default_reward_team", defaultPerTeamReward);
		json.addProperty("default_consume_items", defaultTeamConsumeItems);
		json.addProperty("default_autoclaim_rewards", defaultRewardAutoClaim.getId());
		json.addProperty("default_quest_shape", defaultQuestShape);
		json.addProperty("default_quest_disable_jei", defaultQuestDisableJEI);
		if (!emergencyItems.isEmpty()) {
			Json5Util.store(json, "emergency_items", ItemStack.CODEC.listOf(), emergencyItems);
		}
		json.addProperty("emergency_items_cooldown", emergencyItemsCooldown);
		json.addProperty("drop_loot_crates", dropLootCrates);
		Json5Util.store(json, "loot_crate_no_drop", EntityWeight.CODEC, lootCrateNoDrop);
		json.addProperty("disable_gui", disableGui);
		json.addProperty("grid_scale", gridScale);
		json.addProperty("pause_game", pauseGame);
		json.addProperty("lock_message", lockMessage);
		json.addProperty("progression_mode", progressionMode.getId());
		json.addProperty("detection_delay", detectionDelay);
		json.addProperty("show_lock_icons", showLockIcons);
		json.addProperty("drop_book_on_death", dropBookOnDeath);
		json.addProperty("hide_excluded_quests", hideExcludedQuests);
		json.addProperty("fallback_locale", fallbackLocale);
		json.addProperty("verify_on_load", verifyOnLoad);
	}

	@Override
	public final void readData(@UnknownNullability Json5Object json, HolderLookup.Provider provider) {
		super.readData(json, provider);

		defaultPerTeamReward = Json5Util.getBoolean(json, "default_reward_team").orElseThrow();
		defaultTeamConsumeItems = Json5Util.getBoolean(json, "default_consume_items").orElseThrow();
		defaultRewardAutoClaim = RewardAutoClaim.NAME_MAP_NO_DEFAULT.get(json.get("default_autoclaim_rewards").getAsString());
		defaultQuestShape = Json5Util.getString(json, "default_quest_shape").orElseThrow();
		if (defaultQuestShape.equals("default")) {
			defaultQuestShape = "";
		}
		defaultQuestDisableJEI = Json5Util.getBoolean(json, "default_quest_disable_jei").orElseThrow();

		emergencyItems.clear();
		Json5Util.fetch(json, "emergency_items", ItemStack.CODEC.listOf()).ifPresent(emergencyItems::addAll);

		emergencyItemsCooldown = Json5Util.getInt(json, "emergency_items_cooldown").orElseThrow();
		dropLootCrates = Json5Util.getBoolean(json, "drop_loot_crates").orElseThrow();
		lootCrateNoDrop = Json5Util.fetch(json, "loot_crate_no_drop", EntityWeight.CODEC).orElseGet(EntityWeight::zero);
		disableGui = Json5Util.getBoolean(json, "disable_gui").orElse(false);
		gridScale = Json5Util.getDouble(json, "grid_scale").orElse(0.5D);
		pauseGame = Json5Util.getBoolean(json, "pause_game").orElse(false);
		lockMessage = Json5Util.getString(json, "lock_message").orElse("");
		progressionMode = Json5Util.getString(json, "progression_mode").map(ProgressionMode.NAME_MAP_NO_DEFAULT::get).orElse(ProgressionMode.LINEAR);
		detectionDelay = Json5Util.getInt(json, "detection_delay").orElse(20);
		showLockIcons = Json5Util.getBoolean(json, "show_lock_icons").orElse(false);
		dropBookOnDeath = Json5Util.getBoolean(json, "drop_book_on_death").orElse(false);
		hideExcludedQuests = Json5Util.getBoolean(json, "hide_excluded_quests").orElse(false);
		fallbackLocale = Json5Util.getString(json, "fallback_locale").orElse(TranslationManager.DEFAULT_FALLBACK_LOCALE);
		verifyOnLoad = Json5Util.getBoolean(json, "verify_on_load").orElse(false);
	}

	public final void writeDataFull(Path folder, HolderLookup.Provider provider) {
		boolean prev = false;
		try {
			// Sorting keys ensure consistent sort order in the saved quest file
			// Since questbook data is commonly stored under version control, this minimizes extraneous
			//  version control changes stemming from unpredictable hashmap key ordering
//			prev = SNBT.setShouldSortKeysOnWrite(true);

			Json5Util.tryWrite(folder.resolve("data" + FILE_SUFFIX), Util.make(new Json5Object(), j -> {
				j.addProperty("version", VERSION);
				writeData(j, provider);
			}));

			writeChapterJsonFiles(folder, provider);
			writeRewardTableFiles(folder, provider);
			writeChapterGroupsFile(folder, provider);
		} catch (IOException e) {
			LOGGER.error("Failed to save quest file.", e);
		} finally {
//			SNBT.setShouldSortKeysOnWrite(prev);
		}
	}

	private void writeChapterJsonFiles(Path folder, HolderLookup.Provider provider) throws IOException {
		for (ChapterGroup group : chapterGroups) {
			for (int idx = 0; idx < group.getChapters().size(); idx++) {
				Chapter chapter = group.getChapters().get(idx);
				Json5Object chapterJson = new Json5Object();
				chapterJson.addProperty("id", chapter.getCodeString());
				chapterJson.addProperty("group", group.isDefaultGroup() ? "" : group.getCodeString());
				chapterJson.addProperty("order_index", idx);
				chapter.writeData(chapterJson, provider);

				chapterJson.add("quests", Util.make(new Json5Array(), list -> {
					for (Quest quest : chapter.getQuests()) {
						if (quest.isValid()) {
							list.add(quest.writeDataFull(provider));
						}
					}
				}));

				chapterJson.add("quest_links", Util.make(new Json5Array(), list -> {
					for (QuestLink link : chapter.getQuestLinks()) {
						if (link.getQuest().isPresent()) {
							list.add(Util.make(new Json5Object(), linkNBT -> {
								link.writeData(linkNBT, provider);
								linkNBT.addProperty("id", link.getCodeString());
							}));
						}
					}
				}));

				chapterJson.add("images", Util.make(new Json5Array(), list -> {
					for (ChapterImage image : chapter.getImages()) {
						list.add(Util.make(new Json5Object(), imageNBT -> {
							image.writeData(imageNBT, provider);
							imageNBT.addProperty("id", image.getCodeString());
						}));
					}
				}));

				Json5Util.tryWrite(folder.resolve("chapters").resolve(chapter.getFilename() + FILE_SUFFIX), chapterJson);
			}
		}
	}

	private void writeRewardTableFiles(Path folder, HolderLookup.Provider provider) throws IOException {
		for (int ri = 0; ri < rewardTables.size(); ri++) {
			RewardTable table = rewardTables.get(ri);
			Json5Object tableNBT = new Json5Object();
			tableNBT.addProperty("id", table.getCodeString());
			tableNBT.addProperty("order_index", ri);
			table.writeData(tableNBT, provider);
			Json5Util.tryWrite(folder.resolve("reward_tables").resolve(table.getFilename() + FILE_SUFFIX), tableNBT);
		}
	}

	private void writeChapterGroupsFile(Path folder, HolderLookup.Provider provider) throws IOException {
		Json5Array chapterGroupJson = new Json5Array();
		for (ChapterGroup group : chapterGroups) {
			if (!group.isDefaultGroup()) {
				Json5Object groupTag = new Json5Object();
//					groupTag.singleLine();
				groupTag.addProperty("id", group.getCodeString());
				group.writeData(groupTag, provider);
				chapterGroupJson.add(groupTag);
			}
		}
		Json5Object groupJson = new Json5Object();
		groupJson.add("chapter_groups", chapterGroupJson);
		Json5Util.tryWrite(folder.resolve("chapter_groups" + FILE_SUFFIX), groupJson);
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

		final Long2ObjectOpenHashMap<Json5Object> dataCache = new Long2ObjectOpenHashMap<>();
		try {
			var fileJson = Json5Util.tryRead(folder.resolve("data" + FILE_SUFFIX));
			fileVersion = Json5Util.getInt(fileJson, "version").orElseThrow();
			questObjectMap.put(1, this);
			readData(fileJson, provider);
		} catch (IOException e) {
			LOGGER.error("Failed to load default data file.", e);
		}

		translationManager.loadFromFile(this, folder.resolve("lang"));

		readChapterGroupsFile(folder, dataCache);

		Path chaptersFolder = folder.resolve("chapters");

		Long2IntOpenHashMap objectOrderMap = new Long2IntOpenHashMap();
		objectOrderMap.defaultReturnValue(-1);

		if (Files.exists(chaptersFolder)) {
			try (Stream<Path> s = Files.list(chaptersFolder)) {
				s.filter(path -> path.toString().endsWith(FILE_SUFFIX)).forEach(path -> {
					try {
						var chapterJson = Json5Util.tryRead(path);
						Chapter chapter = new Chapter(
								readID(chapterJson.get("id")),
								this,
								getChapterGroup(getID(chapterJson.get("group"))),
								path.getFileName().toString().replace(FILE_SUFFIX, "")
						);
						objectOrderMap.put(chapter.id, (int) Json5Util.getInt(chapterJson, "order_index").orElse(0));
						questObjectMap.put(chapter.id, chapter);
						dataCache.put(chapter.id, chapterJson);
						chapter.getGroup().addChapter(chapter);
						chapterCounter.increment();

						Json5Util.getJson5Array(chapterJson, "quests").ifPresent(questList -> {
							readQuestsFromChapterJson(chapter, questList, dataCache);
							questCounter.add(questList.size());
						});
						Json5Util.getJson5Array(chapterJson, "quest_links").ifPresent(questLinks ->
								readQuestLinksFromChapterJson(chapter, questLinks, dataCache));
						Json5Util.getJson5Array(chapterJson, "images").ifPresent(images ->
								readImagesFromChapterJson(chapter, images, dataCache));
					} catch (IOException e) {
						LOGGER.error("Failed to load chapter {}", path, e);
					}
				});
			} catch (IOException e) {
				LOGGER.error("Failed to read chapters folder.", e);
			}
		}

		Path rewardTableFolder = folder.resolve("reward_tables");
		if (Files.exists(rewardTableFolder)) {
			try (Stream<Path> s = Files.list(rewardTableFolder)) {
				s.filter(path -> path.toString().endsWith(FILE_SUFFIX))
						.forEach(path -> loadRewardTableFile(path, objectOrderMap, dataCache));
			} catch (Exception ex) {
				FTBQuests.LOGGER.error("failed to load reward table data: {}", ex.getMessage());
			}
		}

		for (QuestObjectBase object : questObjectMap.values()) {
			var data = dataCache.get(object.id);
			//noinspection ConstantValue
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

		refreshRewardTableRewardIDs();

		if (verifyOnLoad) {
			forAllQuests(q -> q.verifyDependencies(false));
		}

		for (QuestObjectBase object : getAllObjects()) {
			if (object instanceof CustomTask customTask) {
				NativeEventPosting.get().postEvent(new CustomTaskEvent.Data(customTask));
			}
		}

		if (fileVersion != VERSION) {
			markDirty();
		}

		FTBQuests.LOGGER.info("Loaded {} chapter groups, {} chapters, {} quests, {} reward tables", chapterGroups.size(), chapterCounter, questCounter, rewardTables.size());
	}

	private void loadRewardTableFile(Path path, Long2IntOpenHashMap objectOrderMap, Long2ObjectOpenHashMap<Json5Object> dataCache) {
		try {
			var tableJson = Json5Util.tryRead(path);
			String filename = path.getFileName().toString().replace(FILE_SUFFIX, "");

			RewardTable table = new RewardTable(readID(tableJson.get("id")), this, filename);
			objectOrderMap.put(table.id, (int) Json5Util.getInt(tableJson, "order_index").orElse(0));
			questObjectMap.put(table.id, table);
			dataCache.put(table.id, tableJson);

			rewardTables.add(table);
		} catch (IOException e) {
			LOGGER.error("Failed to load reward table {}", path, e);
		}
	}

	private void readQuestsFromChapterJson(Chapter chapter, Json5Array questList, Long2ObjectOpenHashMap<Json5Object> dataCache) {
		for (var el : questList) {
			if (el instanceof Json5Object questJson) {
				Quest quest = new Quest(readID(questJson.get("id")), chapter);
				questObjectMap.put(quest.id, quest);
				dataCache.put(quest.id, questJson);
				chapter.addQuest(quest);

				Json5Util.getJson5Array(questJson, "tasks").ifPresent(taskList -> {
					for (var taskEl : taskList) {
						if (taskEl instanceof Json5Object taskJson) {
							long taskId = readID(taskJson.get("id"));
							Task task = TaskType.createTask(taskId, quest, Json5Util.getString(taskJson, "type").orElseThrow());
							questObjectMap.put(task.id, task);
							dataCache.put(task.id, taskJson);
							quest.addTask(task);
						}
					}
				});

				Json5Util.getJson5Array(questJson, "rewards").ifPresent(rewardList -> {
					for (var rewardEl : rewardList) {
						if (rewardEl instanceof Json5Object rewardJson) {
							long rewardId = readID(rewardJson.get("id"));
							Reward reward = RewardType.createReward(rewardId, quest, Json5Util.getString(rewardJson, "type").orElseThrow());
							questObjectMap.put(reward.id, reward);
							dataCache.put(reward.id, rewardJson);
							quest.addReward(reward);
						}
					}
				});
			}
		}
	}

	private void readQuestLinksFromChapterJson(Chapter chapter, Json5Array questLinks, Long2ObjectOpenHashMap<Json5Object> dataCache) {
		for (var e : questLinks) {
			if (e instanceof Json5Object linkNBT) {
				QuestLink link = new QuestLink(readID(linkNBT.get("id")), chapter, readID(linkNBT.get("linked_quest")));
				chapter.addQuestLink(link);
				questObjectMap.put(link.id, link);
				dataCache.put(link.id, linkNBT);
			}
		}
	}

	private void readImagesFromChapterJson(Chapter chapter, Json5Array images, Long2ObjectOpenHashMap<Json5Object> dataCache) {
		for (var e : images) {
			if (e instanceof Json5Object imgNBT) {
				// will generate a new id if the "id" field is missing, good for loading older data
				ChapterImage image = new ChapterImage(readID(imgNBT.get("id")), chapter);
				chapter.addImage(image);
				questObjectMap.put(image.id, image);
				dataCache.put(image.id, imgNBT);
			}
		}
	}

	private void readChapterGroupsFile(Path folder, Long2ObjectOpenHashMap<Json5Object> dataCache) {
		Path groupsFile = folder.resolve("chapter_groups" + FILE_SUFFIX);
		if (Files.exists(groupsFile)) {
			try {
				var chapterGroupsJson = Json5Util.tryRead(groupsFile);
				Json5Util.getJson5Array(chapterGroupsJson, "chapter_groups").ifPresent(groups ->
						groups.forEach(el -> {
							if (el instanceof Json5Object groupJson) {
								ChapterGroup chapterGroup = new ChapterGroup(readID(groupJson.get("id")), this);
								questObjectMap.put(chapterGroup.id, chapterGroup);
								dataCache.put(chapterGroup.id, groupJson);
								chapterGroups.add(chapterGroup);
							}
						}));
			} catch (IOException e) {
				LOGGER.error("Failed to read chapter groups file.", e);
			}
		}
	}

	public void updateLootCrates() {
		Map<String, LootCrate> lootCrates = LootCrate.getLootCrates(!isServerSide());
		Set<String> prevCrateNames = new HashSet<>(lootCrates.keySet());
		Collection<ItemStack> oldStacks = LootCrate.allCrateStacks(!isServerSide());

		lootCrates.clear();
		for (RewardTable table : rewardTables) {
			if (table.getLootCrate() != null) {
				lootCrates.put(table.getLootCrate().getStringID(), table.getLootCrate());
			}
		}

		if (!isServerSide() && !prevCrateNames.equals(lootCrates.keySet())) {
			FTBQuestsClient.rebuildCreativeTabs();
			FTBQuests.getRecipeModHelper().updateItemsDynamic(oldStacks, LootCrate.allCrateStacks(!isServerSide()));
		}

		FTBQuests.LOGGER.debug("Updated loot crates (was {}, now {})", prevCrateNames.size(), lootCrates.size());
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
		EntityWeight.STREAM_CODEC.encode(buffer, lootCrateNoDrop);
		buffer.writeBoolean(disableGui);
		buffer.writeDouble(gridScale);
		buffer.writeBoolean(pauseGame);
		buffer.writeUtf(lockMessage, Short.MAX_VALUE);
		ProgressionMode.NAME_MAP_NO_DEFAULT.write(buffer, progressionMode);
		buffer.writeVarInt(detectionDelay);
		buffer.writeBoolean(showLockIcons);
		buffer.writeBoolean(dropBookOnDeath);
		buffer.writeBoolean(hideExcludedQuests);
		buffer.writeUtf(fallbackLocale);
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
		lootCrateNoDrop = EntityWeight.STREAM_CODEC.decode(buffer);
		disableGui = buffer.readBoolean();
		gridScale = buffer.readDouble();
		pauseGame = buffer.readBoolean();
		lockMessage = buffer.readUtf(Short.MAX_VALUE);
		progressionMode = ProgressionMode.NAME_MAP_NO_DEFAULT.read(buffer);
		detectionDelay = buffer.readVarInt();
		showLockIcons = buffer.readBoolean();
		dropBookOnDeath = buffer.readBoolean();
		hideExcludedQuests = buffer.readBoolean();
		fallbackLocale = buffer.readUtf();
	}

	public final void writeNetDataFull(RegistryFriendlyByteBuf buffer) {
		int pos = buffer.writerIndex();

		buffer.writeVarInt(TaskTypes.TYPES.size());
		for (TaskType type : TaskTypes.TYPES.values()) {
			buffer.writeIdentifier(type.getTypeId());
			buffer.writeVarInt(type.internalId);
		}

		buffer.writeVarInt(RewardTypes.TYPES.size());
		for (RewardType type : RewardTypes.TYPES.values()) {
			buffer.writeIdentifier(type.getTypeId());
			buffer.writeVarInt(type.internalId);
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
						buffer.writeVarInt(reward.getType().internalId);
						buffer.writeLong(reward.id);
					});
				}

				buffer.writeVarInt(chapter.getQuestLinks().size());
				for (QuestLink questLink : chapter.getQuestLinks()) {
					buffer.writeLong(questLink.id);
				}

				buffer.writeVarInt(chapter.getImages().size());
				for (ChapterImage img : chapter.getImages()) {
					buffer.writeLong(img.id);
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
				chapter.getImages().forEach(image -> image.writeNetData(buffer));
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
			type.internalId = 0;
		}

		int taskTypesSize = buffer.readVarInt();
		for (int i = 0; i < taskTypesSize; i++) {
			TaskType type = TaskTypes.TYPES.get(buffer.readIdentifier());
			int id = buffer.readVarInt();

			if (type != null) {
				type.internalId = id;
				taskTypeIds.put(type.internalId, type);
			}
		}

		int rewardTypesSize = buffer.readVarInt();
		for (int i = 0; i < rewardTypesSize; i++) {
			RewardType type = RewardTypes.TYPES.get(buffer.readIdentifier());
			int id = buffer.readVarInt();

			if (type != null) {
				type.internalId = id;
				rewardTypeIds.put(type.internalId, type);
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

				int imageCount = buffer.readVarInt();
				for (int j = 0; j < imageCount; j++) {
					ChapterImage image = new ChapterImage(buffer.readLong(), chapter);
					chapter.addImage(image);
				}
			}
		}

		for (RewardTable table : rewardTables) {
			table.readNetData(buffer);
		}

		refreshIDMap();

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

				for (ChapterImage image : chapter.getImages()) {
					image.readNetData(buffer);
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
		return teamDataMap.computeIfAbsent(teamId, _ -> new TeamData(teamId, isServerSide()));
	}

	@Override
	public TeamData getOrCreateTeamData(Team team) {
		return getOrCreateTeamData(Objects.requireNonNull(team, "Non-null team required!").getId());
	}

	@Override
	public Optional<TeamData> getTeamData(Player player) {
		return player.level().isClientSide() ?
				getClientTeamData(player) :
				FTBTeamsAPI.api().getManager().getTeamForPlayerID(player.getUUID())
						.map(this::getOrCreateTeamData);
	}

	private Optional<TeamData> getClientTeamData(Player player) {
		ClientTeamManager mgr = FTBTeamsAPI.api().getClientManager();
		return mgr.getKnownPlayer(player.getUUID())
				.map(kcp -> mgr.getTeamByID(kcp.teamId()))
				.flatMap(team -> team.map(this::getOrCreateTeamData));
	}

	@Override
	public Collection<TeamData> getAllTeamData() {
		return Collections.unmodifiableCollection(teamDataMap.values());
	}

	public abstract void deleteObject(long id);

	@Override
	public MutableComponent getAltTitle() {
		return Component.translatable("ftbquests.file");
	}

	@Override
	public Icon<?> getAltIcon() {
		return ThemeProperties.MODPACK_ICON.get(this);
	}

	@Override
	public void fillConfigGroup(EditableConfigGroup config) {
		super.fillConfigGroup(config);
		config.addList("emergency_items", emergencyItems, new EditableItemStack(false, false), ItemStack.EMPTY);
		config.addInt("emergency_items_cooldown", emergencyItemsCooldown, v -> emergencyItemsCooldown = v, 300, 0, Integer.MAX_VALUE);
		config.addBool("drop_loot_crates", dropLootCrates, v -> dropLootCrates = v, false);
		config.addBool("disable_gui", disableGui, v -> disableGui = v, false);
		config.addDouble("grid_scale", gridScale, v -> gridScale = v, 0.5D, 1D / 32D, 8D);
		config.addString("lock_message", lockMessage, v -> lockMessage = v, "");
		config.addEnum("progression_mode", progressionMode, v -> progressionMode = v, ProgressionMode.NAME_MAP_NO_DEFAULT);
		config.addInt("detection_delay", detectionDelay, v -> detectionDelay = v, 20, 0, 200);
		config.addBool("pause_game", pauseGame, v -> pauseGame = v, false);
		config.addBool("show_lock_icons", showLockIcons, v -> showLockIcons = v, true).setNameKey("ftbquests.ui.show_lock_icon");
		config.addBool("drop_book_on_death", dropBookOnDeath, v -> dropBookOnDeath = v, true);
		config.addBool("hide_excluded_quests", hideExcludedQuests, v -> hideExcludedQuests = v, false);
		config.add("fallback_locale", new EditableLocaleConfig(), fallbackLocale, v -> fallbackLocale = v, "");

		EditableConfigGroup defaultsGroup = config.getOrCreateSubgroup("defaults");
		defaultsGroup.addBool("reward_team", defaultPerTeamReward, v -> defaultPerTeamReward = v, false);
		defaultsGroup.addBool("consume_items", defaultTeamConsumeItems, v -> defaultTeamConsumeItems = v, false);
		defaultsGroup.addEnum("autoclaim_rewards", defaultRewardAutoClaim, v -> defaultRewardAutoClaim = v, RewardAutoClaim.NAME_MAP_NO_DEFAULT);
		defaultsGroup.addEnum("quest_shape", defaultQuestShape, v -> defaultQuestShape = v, QuestShape.idMap);
		defaultsGroup.addBool("quest_disable_jei", defaultQuestDisableJEI, v -> defaultQuestDisableJEI = v, false);

		EditableConfigGroup d = config.getOrCreateSubgroup("loot_crate_no_drop");
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

		NativeEventPosting.get().postEvent(new ClearFileCacheEvent.Data(this));
	}

	public void clearCachedProgress() {
		getAllTeamData().forEach(TeamData::clearCachedProgress);
	}

	public long newID() {
		return readID(0L);
	}

	public long readID(long id) {
		//noinspection ConstantValue
		while (id == 0L || id == 1L || questObjectMap.get(id) != null) {
			id = Math.abs(MathUtils.RAND.nextLong());
			markDirty();
		}

		return id;
	}

	public long readID(@Nullable Json5Element tag) {
		if (tag instanceof Json5Primitive p) {
			if (p.isNumber()) {
				markDirty();
				return readID(tag.getAsLong());
			} else if (p.isString()) {
				try {
					String id = tag.getAsString();
					return readID(Long.parseLong(id.charAt(0) == '#' ? id.substring(1) : id, 16));
				} catch (Exception ignored) {
				}
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
			case Json5Primitive p -> {
				return getID(p.getAsString());
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
	public Chapter getFirstVisibleChapter(@Nullable TeamData data) {
		if (data != null) {
			for (ChapterGroup group : chapterGroups) {
				Chapter c = group.getFirstVisibleChapter(data);

				if (c != null) {
					return c;
				}
			}
		}

		return null;
	}

	public <T extends QuestObjectBase> List<T> collect(Class<T> cls, Predicate<T> filter) {
		List<T> list = new ArrayList<>();

		for (QuestObjectBase base : getAllObjects()) {
			if (cls.isAssignableFrom(base.getClass())) {
				T casted = cls.cast(base);
				if (filter.test(casted)) {
					list.add(casted);
				}
			}
		}

		if (list.isEmpty()) {
			return Collections.emptyList();
		} else if (list.size() == 1) {
			return Collections.singletonList(list.getFirst());
		}

		return list;
	}

	public <T extends QuestObjectBase> List<T> collect(Class<T> clazz) {
		return collect(clazz, _ -> true);
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

	public boolean isHideExcludedQuests() {
		return hideExcludedQuests;
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

	public boolean showLockIcons() {
		return showLockIcons;
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
				Path path = ServerQuestFile.getInstance().getFolder().resolve(table.getPath().orElseThrow());
				try {
					Files.delete(path);
					del.increment();
					table.invalid = true;
					Server2PlayNetworking.sendToAllPlayers(source.getServer(), new DeleteObjectResponseMessage(table.id));
				} catch (IOException e) {
					FTBQuests.LOGGER.error("can't delete file {}: {}", path, e.getMessage());
				}
			}
		}

		if (rewardTables.removeIf(rewardTable -> rewardTable.invalid)) {
			refreshIDMap();
			markDirty();
		}

		return del.intValue();
	}

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

	public String getFallbackLocale() {
		return fallbackLocale.isEmpty() ? TranslationManager.DEFAULT_FALLBACK_LOCALE : fallbackLocale;
	}

	public boolean dropBookOnDeath() {
		return dropBookOnDeath;
	}
}
