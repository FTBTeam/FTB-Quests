package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.events.ClearFileCacheEvent;
import com.feed_the_beast.ftbquests.events.CustomTaskEvent;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.item.MissingItem;
import com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;
import com.feed_the_beast.ftbquests.net.MessageDisplayCompletionToast;
import com.feed_the_beast.ftbquests.quest.loot.EntityWeight;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.reward.CustomReward;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardAutoClaim;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.reward.RewardTypes;
import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.feed_the_beast.ftbquests.quest.task.TaskTypes;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.ftbquests.util.NBTUtils;
import com.feed_the_beast.ftbquests.util.NetUtils;
import com.feed_the_beast.ftbquests.util.OrderedCompoundTag;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigItemStack;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.MathUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.shedaniel.architectury.utils.Env;
import me.shedaniel.architectury.utils.NbtType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public abstract class QuestFile extends QuestObject {
	public static final int VERSION = 11;

	public final DefaultChapterGroup defaultChapterGroup;
	public final List<ChapterGroup> chapterGroups;
	public final List<RewardTable> rewardTables;
	protected final Map<UUID, PlayerData> playerDataMap;

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

	public QuestFile() {
		id = 1;
		fileVersion = 0;
		defaultChapterGroup = new DefaultChapterGroup(this);
		chapterGroups = new ArrayList<>();
		chapterGroups.add(defaultChapterGroup);
		rewardTables = new ArrayList<>();
		playerDataMap = new HashMap<>();

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

	public void load(UUID s) {
		throw new IllegalStateException("This method can only be called from client quest file!");
	}

	@Override
	public int getRelativeProgressFromChildren(PlayerData data) {
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
	public void onCompleted(PlayerData data, List<ServerPlayer> onlineMembers, List<ServerPlayer> notifiedPlayers) {
		super.onCompleted(data, onlineMembers, notifiedPlayers);
		ObjectCompletedEvent.FILE.invoker().act(new ObjectCompletedEvent.FileEvent(data, this, onlineMembers, notifiedPlayers));

		if (!disableToast) {
			for (ServerPlayer player : notifiedPlayers) {
				new MessageDisplayCompletionToast(id).sendTo(player);
			}
		}
	}

	@Override
	public void changeProgress(PlayerData data, ChangeProgress type) {
		for (ChapterGroup group : chapterGroups) {
			group.changeProgress(data, type);
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
			if (object instanceof QuestObject) {
				QuestObject o = (QuestObject) object;

				for (ChapterGroup group : chapterGroups) {
					for (Chapter chapter : group.chapters) {
						for (Quest quest : chapter.quests) {
							quest.dependencies.remove(o);
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

				for (Quest quest : chapter.quests) {
					map.put(quest.id, quest);

					for (Task task : quest.tasks) {
						map.put(task.id, task);
					}

					for (Reward reward : quest.rewards) {
						map.put(reward.id, reward);
					}
				}
			}
		}

		clearCachedData();
	}

	public QuestObjectBase create(QuestObjectType type, long parent, CompoundTag extra) {
		switch (type) {
			case CHAPTER:
				return new Chapter(this, getChapterGroup(extra.getLong("group")));
			case QUEST: {
				Chapter chapter = getChapter(parent);

				if (chapter != null) {
					return new Quest(chapter);
				}

				throw new IllegalArgumentException("Parent chapter not found!");
			}
			case TASK: {
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
			case REWARD:
				Quest quest = getQuest(parent);

				if (quest != null) {
					Reward reward = RewardType.createReward(quest, extra.getString("type"));

					if (reward != null) {
						return reward;
					}

					throw new IllegalArgumentException("Unknown reward type!");
				}

				throw new IllegalArgumentException("Parent quest not found!");
			case REWARD_TABLE:
				return new RewardTable(this);
			case CHAPTER_GROUP:
				return new ChapterGroup(this);
			default:
				throw new IllegalArgumentException("Unknown type: " + type);
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

		CompoundTag lootCrateNoDropTag = new OrderedCompoundTag();
		lootCrateNoDrop.writeData(lootCrateNoDropTag);
		nbt.put("loot_crate_no_drop", lootCrateNoDropTag);
		nbt.putBoolean("disable_gui", disableGui);
		nbt.putDouble("grid_scale", gridScale);
		nbt.putBoolean("pause_game", pauseGame);
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

		ListTag emergencyItemsTag = nbt.getList("emergency_items", NbtType.COMPOUND);

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
	}

	public final void writeDataFull(Path folder) {
		CompoundTag fileNBT = new OrderedCompoundTag();
		fileNBT.putInt("version", VERSION);
		writeData(fileNBT);
		NBTUtils.writeSNBT(folder.resolve("data.snbt"), fileNBT);

		for (ChapterGroup group : chapterGroups) {
			for (int ci = 0; ci < group.chapters.size(); ci++) {
				Chapter chapter = group.chapters.get(ci);
				CompoundTag chapterNBT = new OrderedCompoundTag();
				chapterNBT.putString("id", chapter.getCodeString());
				chapterNBT.putString("group", group.isDefaultGroup() ? "" : group.getCodeString());
				chapterNBT.putInt("order_index", ci);
				chapter.writeData(chapterNBT);

				ListTag questList = new ListTag();

				for (Quest quest : chapter.quests) {
					if (quest.invalid) {
						continue;
					}

					CompoundTag questNBT = new OrderedCompoundTag();
					quest.writeData(questNBT);
					questNBT.putString("id", quest.getCodeString());

					if (!quest.tasks.isEmpty()) {
						ListTag t = new ListTag();

						for (Task task : quest.tasks) {
							TaskType type = task.getType();
							CompoundTag nbt3 = new OrderedCompoundTag();
							nbt3.putString("id", task.getCodeString());
							nbt3.putString("type", type.getTypeForNBT());
							task.writeData(nbt3);
							t.add(nbt3);
						}

						if (!t.isEmpty()) {
							questNBT.put("tasks", t);
						}
					}

					if (!quest.rewards.isEmpty()) {
						ListTag r = new ListTag();

						for (Reward reward : quest.rewards) {
							RewardType type = reward.getType();
							CompoundTag nbt3 = new OrderedCompoundTag();
							nbt3.putString("id", reward.getCodeString());
							nbt3.putString("type", type.getTypeForNBT());
							reward.writeData(nbt3);
							r.add(nbt3);
						}

						if (!r.isEmpty()) {
							questNBT.put("rewards", r);
						}
					}

					questList.add(questNBT);
				}

				chapterNBT.put("quests", questList);
				NBTUtils.writeSNBT(folder.resolve("chapters/" + chapter.getFilename() + ".snbt"), chapterNBT);
			}
		}

		for (int ri = 0; ri < rewardTables.size(); ri++) {
			RewardTable table = rewardTables.get(ri);
			CompoundTag tableNBT = new OrderedCompoundTag();
			tableNBT.putString("id", table.getCodeString());
			tableNBT.putInt("order_index", ri);
			table.writeData(tableNBT);
			NBTUtils.writeSNBT(folder.resolve("reward_tables/" + table.getFilename() + ".snbt"), tableNBT);
		}

		ListTag chapterGroupTag = new ListTag();

		for (ChapterGroup group : chapterGroups) {
			if (!group.isDefaultGroup()) {
				CompoundTag groupTag = new OrderedCompoundTag();
				groupTag.putString("id", group.getCodeString());
				group.writeData(groupTag);
				chapterGroupTag.add(groupTag);
			}
		}

		CompoundTag groupNBT = new OrderedCompoundTag();
		groupNBT.put("chapter_groups", chapterGroupTag);
		NBTUtils.writeSNBT(folder.resolve("chapter_groups.snbt"), groupNBT);
	}

	public final void readDataFull(Path folder) {
		clearCachedData();
		map.clear();
		defaultChapterGroup.chapters.clear();
		chapterGroups.clear();
		chapterGroups.add(defaultChapterGroup);
		rewardTables.clear();

		final Long2ObjectOpenHashMap<CompoundTag> dataCache = new Long2ObjectOpenHashMap<>();
		CompoundTag fileNBT = NBTUtils.readSNBT(folder.resolve("data.snbt"));

		if (fileNBT != null) {
			fileVersion = fileNBT.getInt("version");
			map.put(1, this);
			readData(fileNBT);
		}

		Path groupsFile = folder.resolve("chapter_groups.snbt");

		if (Files.exists(groupsFile)) {
			CompoundTag chapterGroupsTag = NBTUtils.readSNBT(groupsFile);

			if (chapterGroupsTag != null) {
				ListTag groupListTag = chapterGroupsTag.getList("chapter_groups", NbtType.COMPOUND);

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
			try {
				Files.list(chaptersFolder).forEach(path -> {
					CompoundTag chapterNBT = NBTUtils.readSNBT(path);

					if (chapterNBT != null) {
						Chapter chapter = new Chapter(this, getChapterGroup(getID(chapterNBT.get("group"))));
						chapter.id = readID(chapterNBT.get("id"));
						chapter.filename = path.getFileName().toString().replace(".snbt", "");
						objectOrderMap.put(chapter.id, chapterNBT.getInt("order_index"));
						map.put(chapter.id, chapter);
						dataCache.put(chapter.id, chapterNBT);
						chapter.group.chapters.add(chapter);

						ListTag questList = chapterNBT.getList("quests", NbtType.COMPOUND);

						for (int i = 0; i < questList.size(); i++) {
							CompoundTag questNBT = questList.getCompound(i);
							Quest quest = new Quest(chapter);
							quest.id = readID(questNBT.get("id"));
							map.put(quest.id, quest);
							dataCache.put(quest.id, questNBT);
							chapter.quests.add(quest);

							ListTag taskList = questNBT.getList("tasks", NbtType.COMPOUND);

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

							ListTag rewardList = questNBT.getList("rewards", NbtType.COMPOUND);

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
						}
					}
				});
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		Path rewardTableFolder = folder.resolve("reward_tables");

		if (Files.exists(rewardTableFolder)) {
			try {
				Files.list(rewardTableFolder).forEach(path -> {
					CompoundTag tableNBT = NBTUtils.readSNBT(path);

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
				for (Quest quest : chapter.quests) {
					quest.removeInvalidDependencies();
				}
			}
		}

		rewardTables.sort(Comparator.comparingInt(c -> objectOrderMap.get(c.id)));

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
	}

	public final void writeNetDataFull(FriendlyByteBuf buffer, UUID self) {
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
				buffer.writeVarInt(chapter.quests.size());

				for (Quest quest : chapter.quests) {
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

				for (Quest quest : chapter.quests) {
					quest.writeNetData(buffer);

					for (Task task : quest.tasks) {
						task.writeNetData(buffer);
					}

					for (Reward reward : quest.rewards) {
						reward.writeNetData(buffer);
					}
				}
			}
		}

		PlayerData selfPlayerData = getData(self);
		buffer.writeVarInt(playerDataMap.size());

		for (PlayerData data : playerDataMap.values()) {
			NetUtils.writeUUID(buffer, data.uuid);
			data.write(buffer, data == selfPlayerData);
		}

		FTBQuests.LOGGER.debug("Wrote " + (buffer.writerIndex() - pos) + " bytes, " + map.size() + " objects");
	}

	public final void readNetDataFull(FriendlyByteBuf buffer, UUID self) {
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

		int rtl = buffer.readVarInt();

		for (int i = 0; i < rtl; i++) {
			RewardTable table = new RewardTable(this);
			table.id = buffer.readLong();
			rewardTables.add(table);
		}

		chapterGroups.clear();
		chapterGroups.add(defaultChapterGroup);

		int cgl = buffer.readVarInt();

		for (int i = 0; i < cgl; i++) {
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

				int q = buffer.readVarInt();

				for (int j = 0; j < q; j++) {
					Quest quest = new Quest(chapter);
					quest.id = buffer.readLong();
					chapter.quests.add(quest);

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

				for (Quest quest : chapter.quests) {
					quest.readNetData(buffer);

					for (Task task : quest.tasks) {
						task.readNetData(buffer);
					}

					for (Reward reward : quest.rewards) {
						reward.readNetData(buffer);
					}
				}
			}
		}

		int pds = buffer.readVarInt();

		for (int i = 0; i < pds; i++) {
			PlayerData data = new PlayerData(this, NetUtils.readUUID(buffer));
			addData(data, true);
			data.read(buffer, data.uuid.equals(self));
		}

		FTBQuests.LOGGER.info("Read " + (buffer.readerIndex() - pos) + " bytes, " + map.size() + " objects");
	}

	@Override
	public long getParentID() {
		return 0L;
	}

	@Nullable
	public PlayerData getNullablePlayerData(UUID id) {
		return playerDataMap.get(id);
	}

	public PlayerData getData(UUID id) {
		return playerDataMap.computeIfAbsent(id, i -> new PlayerData(this, i));
	}

	public PlayerData getData(Entity player) {
		return getData(player.getUUID());
	}

	public Collection<PlayerData> getAllData() {
		return playerDataMap.values();
	}

	public abstract void deleteObject(long id);

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		return new TranslatableComponent("ftbquests.file");
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
		config.addList("emergency_items", emergencyItems, new ConfigItemStack(false, false), ItemStack.EMPTY);
		config.addInt("emergency_items_cooldown", emergencyItemsCooldown, v -> emergencyItemsCooldown = v, 300, 0, Integer.MAX_VALUE);
		config.addBool("drop_loot_crates", dropLootCrates, v -> dropLootCrates = v, false);
		config.addBool("disable_gui", disableGui, v -> disableGui = v, false);
		config.addDouble("grid_scale", gridScale, v -> gridScale = v, 0.5D, 1D / 32D, 8D);

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

		for (ChapterGroup group : chapterGroups) {
			group.clearCachedData();
		}

		clearCachedProgress();

		ClearFileCacheEvent.EVENT.invoker().accept(new ClearFileCacheEvent(this));
	}

	public void clearCachedProgress() {
		for (PlayerData data : getAllData()) {
			data.clearCache();
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
	public LootCrate getRandomLootCrate(Entity entity, Random random) {
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
	public final int refreshJEI() {
		return FTBQuestsJEIHelper.QUESTS | FTBQuestsJEIHelper.LOOTCRATES;
	}

	public final Collection<QuestObjectBase> getAllObjects() {
		return map.values();
	}

	@Override
	public boolean isVisible(PlayerData data) {
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

	public List<Chapter> getVisibleChapters(PlayerData data) {
		List<Chapter> list = new ArrayList<>();

		for (ChapterGroup group : chapterGroups) {
			list.addAll(group.getVisibleChapters(data));
		}

		return list;
	}

	@Nullable
	public Chapter getFirstVisibleChapter(PlayerData data) {
		List<Chapter> chapters = getVisibleChapters(data);

		if (!chapters.isEmpty()) {
			return chapters.get(0);
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

	public void addData(PlayerData data, boolean strong) {
		playerDataMap.put(data.uuid, data);

		for (ChapterGroup group : chapterGroups) {
			for (Chapter chapter : group.chapters) {
				for (Quest quest : chapter.quests) {
					for (Task task : quest.tasks) {
						data.createTaskData(task, strong);
					}
				}
			}
		}
	}

	public void refreshGui() {
		clearCachedData();
	}
}