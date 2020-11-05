package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.events.ClearFileCacheEvent;
import com.feed_the_beast.ftbquests.events.CustomTaskEvent;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;
import com.feed_the_beast.ftbquests.net.MessageDisplayCompletionToast;
import com.feed_the_beast.ftbquests.quest.loot.EntityWeight;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.reward.CustomReward;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardAutoClaim;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.ftbquests.util.NBTUtils;
import com.feed_the_beast.ftbquests.util.NetUtils;
import com.feed_the_beast.ftbquests.util.OrderedCompoundNBT;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigItemStack;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistry;

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
public abstract class QuestFile extends QuestObject
{
	public static final int VERSION = 9;

	private int lastID;
	public final List<Chapter> chapters;
	public final List<RewardTable> rewardTables;
	protected final Map<UUID, PlayerData> playerDataMap;

	private final Int2ObjectOpenHashMap<QuestObjectBase> map;

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

	public QuestFile()
	{
		id = 1;
		fileVersion = 0;
		lastID = 0;
		chapters = new ArrayList<>();
		rewardTables = new ArrayList<>();
		playerDataMap = new HashMap<>();

		map = new Int2ObjectOpenHashMap<>();

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
	}

	public abstract LogicalSide getSide();

	@Override
	public QuestFile getQuestFile()
	{
		return this;
	}

	@Override
	public QuestObjectType getObjectType()
	{
		return QuestObjectType.FILE;
	}

	public boolean isLoading()
	{
		return false;
	}

	public boolean canEdit()
	{
		return false;
	}

	public Path getFolder()
	{
		throw new IllegalStateException("This quest file doesn't have a folder!");
	}

	public void load(UUID s)
	{
		throw new IllegalStateException("This method can only be called from client quest file!");
	}

	@Override
	public int getRelativeProgressFromChildren(PlayerData data)
	{
		int progress = 0;

		for (Chapter chapter : chapters)
		{
			progress += data.getRelativeProgress(chapter);
		}

		return getRelativeProgressFromChildren(progress, chapters.size());
	}

	@Override
	public void onCompleted(PlayerData data, List<ServerPlayerEntity> onlineMembers, List<ServerPlayerEntity> notifiedPlayers)
	{
		super.onCompleted(data, onlineMembers, notifiedPlayers);
		MinecraftForge.EVENT_BUS.post(new ObjectCompletedEvent.FileEvent(data, this, onlineMembers, notifiedPlayers));

		if (!disableToast)
		{
			for (ServerPlayerEntity player : notifiedPlayers)
			{
				new MessageDisplayCompletionToast(id).sendTo(player);
			}
		}
	}

	@Override
	public void changeProgress(PlayerData data, ChangeProgress type)
	{
		for (Chapter chapter : chapters)
		{
			chapter.changeProgress(data, type);
		}
	}

	@Override
	public void deleteSelf()
	{
		invalid = true;
	}

	@Override
	public void deleteChildren()
	{
		for (Chapter chapter : chapters)
		{
			chapter.deleteChildren();
			chapter.invalid = true;
		}

		chapters.clear();

		for (RewardTable table : rewardTables)
		{
			table.deleteChildren();
			table.invalid = true;
		}

		rewardTables.clear();
	}

	@Nullable
	public QuestObjectBase getBase(int id)
	{
		if (id == 0)
		{
			return null;
		}
		else if (id == 1)
		{
			return this;
		}

		QuestObjectBase object = map.get(id);
		return object == null || object.invalid ? null : object;
	}

	@Nullable
	public QuestObject get(int id)
	{
		QuestObjectBase object = getBase(id);
		return object instanceof QuestObject ? (QuestObject) object : null;
	}

	@Nullable
	public QuestObjectBase remove(int id)
	{
		QuestObjectBase object = map.remove(id);

		if (object != null)
		{
			if (object instanceof QuestObject)
			{
				QuestObject o = (QuestObject) object;

				for (Chapter chapter : chapters)
				{
					for (Quest quest : chapter.quests)
					{
						quest.dependencies.remove(o);
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
	public Chapter getChapter(int id)
	{
		QuestObjectBase object = getBase(id);
		return object instanceof Chapter ? (Chapter) object : null;
	}

	@Nullable
	public Quest getQuest(int id)
	{
		QuestObjectBase object = getBase(id);
		return object instanceof Quest ? (Quest) object : null;
	}

	@Nullable
	public Task getTask(int id)
	{
		QuestObjectBase object = getBase(id);
		return object instanceof Task ? (Task) object : null;
	}

	@Nullable
	public Reward getReward(int id)
	{
		QuestObjectBase object = getBase(id);
		return object instanceof Reward ? (Reward) object : null;
	}

	@Nullable
	public RewardTable getRewardTable(int id)
	{
		QuestObjectBase object = getBase(id);
		return object instanceof RewardTable ? (RewardTable) object : null;
	}

	@Nullable
	public LootCrate getLootCrate(String id)
	{
		if (!id.startsWith("#"))
		{
			for (RewardTable table : rewardTables)
			{
				if (table.lootCrate != null && table.lootCrate.stringID.equals(id))
				{
					return table.lootCrate;
				}
			}
		}

		RewardTable table = getRewardTable(getID(id));
		return table == null ? null : table.lootCrate;
	}

	@Nullable
	public RewardTable getRewardTable(String id)
	{
		LootCrate crate = getLootCrate(id);
		return crate != null ? crate.table : getRewardTable(getID(id));
	}

	public void refreshIDMap()
	{
		clearCachedData();
		map.clear();

		for (RewardTable table : rewardTables)
		{
			map.put(table.id, table);
		}

		for (Chapter chapter : chapters)
		{
			map.put(chapter.id, chapter);

			for (Quest quest : chapter.quests)
			{
				map.put(quest.id, quest);

				for (Task task : quest.tasks)
				{
					map.put(task.id, task);
				}

				for (Reward reward : quest.rewards)
				{
					map.put(reward.id, reward);
				}
			}
		}

		clearCachedData();
	}

	public QuestObjectBase create(QuestObjectType type, int parent, CompoundNBT extra)
	{
		switch (type)
		{
			case CHAPTER:
				return new Chapter(this);
			case QUEST:
			{
				Chapter chapter = getChapter(parent);

				if (chapter != null)
				{
					return new Quest(chapter);
				}

				throw new IllegalArgumentException("Parent chapter not found!");
			}
			case TASK:
			{
				Quest quest = getQuest(parent);

				if (quest != null)
				{
					Task task = TaskType.createTask(quest, extra.getString("type"));

					if (task != null)
					{
						return task;
					}

					throw new IllegalArgumentException("Unknown task type!");
				}

				throw new IllegalArgumentException("Parent quest not found!");
			}
			case REWARD:
				Quest quest = getQuest(parent);

				if (quest != null)
				{
					Reward reward = RewardType.createReward(quest, extra.getString("type"));

					if (reward != null)
					{
						return reward;
					}

					throw new IllegalArgumentException("Unknown reward type!");
				}

				throw new IllegalArgumentException("Parent quest not found!");
			case REWARD_TABLE:
				return new RewardTable(this);
			default:
				throw new IllegalArgumentException("Unknown type: " + type);
		}
	}

	@Override
	public final void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putBoolean("default_reward_team", defaultRewardTeam);
		nbt.putBoolean("default_consume_items", defaultTeamConsumeItems);
		nbt.putString("default_autoclaim_rewards", defaultRewardAutoClaim.id);
		nbt.putString("default_quest_shape", defaultQuestShape);
		nbt.putBoolean("default_quest_disable_jei", defaultQuestDisableJEI);

		if (!emergencyItems.isEmpty())
		{
			ListNBT list = new ListNBT();

			for (ItemStack stack : emergencyItems)
			{
				list.add(stack.serializeNBT());
			}

			nbt.put("emergency_items", list);
		}

		nbt.putInt("emergency_items_cooldown", emergencyItemsCooldown);
		nbt.putBoolean("drop_loot_crates", dropLootCrates);

		CompoundNBT nbt1 = new OrderedCompoundNBT();
		lootCrateNoDrop.writeData(nbt1);
		nbt.put("loot_crate_no_drop", nbt1);
		nbt.putBoolean("disable_gui", disableGui);
		nbt.putDouble("grid_scale", gridScale);
	}

	@Override
	public final void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		defaultRewardTeam = nbt.getBoolean("default_reward_team");
		defaultTeamConsumeItems = nbt.getBoolean("default_consume_items");
		defaultRewardAutoClaim = RewardAutoClaim.NAME_MAP_NO_DEFAULT.get(nbt.getString("default_autoclaim_rewards"));
		defaultQuestShape = nbt.getString("default_quest_shape");

		if (defaultQuestShape.equals("default"))
		{
			defaultQuestShape = "";
		}

		defaultQuestDisableJEI = nbt.getBoolean("default_quest_disable_jei");
		emergencyItems.clear();

		ListNBT list = nbt.getList("emergency_items", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.size(); i++)
		{
			ItemStack stack = NBTUtils.read(list.getCompound(i), "item");

			if (!stack.isEmpty())
			{
				emergencyItems.add(stack);
			}
		}

		emergencyItemsCooldown = nbt.getInt("emergency_items_cooldown");
		dropLootCrates = nbt.getBoolean("drop_loot_crates");

		if (nbt.contains("loot_crate_no_drop"))
		{
			lootCrateNoDrop.readData(nbt.getCompound("loot_crate_no_drop"));
		}

		disableGui = nbt.getBoolean("disable_gui");
		gridScale = nbt.contains("grid_scale") ? nbt.getDouble("grid_scale") : 0.5D;
	}

	public final void writeDataFull(Path folder)
	{
		CompoundNBT fileNBT = new OrderedCompoundNBT();
		fileNBT.putInt("version", VERSION);
		fileNBT.putInt("last_id", lastID);
		writeData(fileNBT);
		NBTUtils.writeSNBT(folder.resolve("data.snbt"), fileNBT);

		for (Chapter chapter : chapters)
		{
			CompoundNBT chapterNBT = new OrderedCompoundNBT();
			chapterNBT.putInt("id", chapter.id);
			chapter.writeData(chapterNBT);

			ListNBT questList = new ListNBT();

			for (Quest quest : chapter.quests)
			{
				if (quest.invalid)
				{
					continue;
				}

				CompoundNBT questNBT = new OrderedCompoundNBT();
				quest.writeData(questNBT);
				questNBT.putInt("id", quest.id);

				if (!quest.tasks.isEmpty())
				{
					ListNBT t = new ListNBT();

					for (Task task : quest.tasks)
					{
						TaskType type = task.getType();
						CompoundNBT nbt3 = new OrderedCompoundNBT();
						nbt3.putInt("id", task.id);
						nbt3.putString("type", type.getTypeForNBT());
						task.writeData(nbt3);
						t.add(nbt3);
					}

					if (!t.isEmpty())
					{
						questNBT.put("tasks", t);
					}
				}

				if (!quest.rewards.isEmpty())
				{
					ListNBT r = new ListNBT();

					for (Reward reward : quest.rewards)
					{
						RewardType type = reward.getType();
						CompoundNBT nbt3 = new OrderedCompoundNBT();
						nbt3.putInt("id", reward.id);
						nbt3.putString("type", type.getTypeForNBT());
						reward.writeData(nbt3);
						r.add(nbt3);
					}

					if (!r.isEmpty())
					{
						questNBT.put("rewards", r);
					}
				}

				questList.add(questNBT);
			}

			chapterNBT.put("quests", questList);
			NBTUtils.writeSNBT(folder.resolve("chapters/" + chapter.filename + ".snbt"), chapterNBT);
		}

		for (RewardTable table : rewardTables)
		{
			CompoundNBT tableNBT = new OrderedCompoundNBT();
			tableNBT.putInt("id", table.id);
			table.writeData(tableNBT);
			NBTUtils.writeSNBT(folder.resolve("reward_tables/" + getCodeString(table) + ".snbt"), tableNBT);
		}
	}

	public final void readDataFull(Path folder)
	{
		clearCachedData();
		map.clear();
		chapters.clear();
		rewardTables.clear();

		final Int2ObjectOpenHashMap<CompoundNBT> dataCache = new Int2ObjectOpenHashMap<>();
		CompoundNBT fileNBT = NBTUtils.readSNBT(folder.resolve("data.snbt"));

		if (fileNBT != null)
		{
			fileVersion = fileNBT.getInt("version");
			lastID = fileNBT.getInt("last_id");
			map.put(1, this);
			dataCache.put(1, fileNBT);
		}

		Path chaptersFolder = folder.resolve("chapters");

		if (Files.exists(chaptersFolder))
		{
			try
			{
				Files.list(chaptersFolder).forEach(path -> {
					CompoundNBT chapterNBT = NBTUtils.readSNBT(path);

					if (chapterNBT != null)
					{
						Chapter chapter = new Chapter(this);
						chapter.orderIndex = chapters.size() + 1;
						chapter.id = readID(chapterNBT.getInt("id"));
						map.put(chapter.id, chapter);
						dataCache.put(chapter.id, chapterNBT);
						chapters.add(chapter);

						ListNBT questList = chapterNBT.getList("quests", Constants.NBT.TAG_COMPOUND);

						for (int i = 0; i < questList.size(); i++)
						{
							CompoundNBT questNBT = questList.getCompound(i);
							Quest quest = new Quest(chapter);
							quest.id = readID(questNBT.getInt("id"));
							map.put(quest.id, quest);
							dataCache.put(quest.id, questNBT);
							chapter.quests.add(quest);

							ListNBT taskList = questNBT.getList("tasks", Constants.NBT.TAG_COMPOUND);

							for (int j = 0; j < taskList.size(); j++)
							{
								CompoundNBT taskNBT = taskList.getCompound(j);
								Task task = TaskType.createTask(quest, taskNBT.getString("type"));

								if (task == null)
								{
									task = new CustomTask(quest);
									task.title = "Unknown type: " + taskNBT.getString("type");
								}

								task.id = readID(taskNBT.getInt("id"));
								map.put(task.id, task);
								dataCache.put(task.id, taskNBT);
								quest.tasks.add(task);
							}

							ListNBT rewardList = questNBT.getList("rewards", Constants.NBT.TAG_COMPOUND);

							for (int j = 0; j < rewardList.size(); j++)
							{
								CompoundNBT rewardNBT = rewardList.getCompound(j);
								Reward reward = RewardType.createReward(quest, rewardNBT.getString("type"));

								if (reward == null)
								{
									reward = new CustomReward(quest);
									reward.title = "Unknown type: " + rewardNBT.getString("type");
								}

								reward.id = readID(rewardNBT.getInt("id"));
								map.put(reward.id, reward);
								dataCache.put(reward.id, rewardNBT);
								quest.rewards.add(reward);
							}
						}
					}
				});
			}
			catch (Exception ex)
			{
			}
		}

		Path rewardTableFolder = folder.resolve("reward_tables");

		if (Files.exists(rewardTableFolder))
		{
			try
			{
				Files.list(rewardTableFolder).forEach(path -> {
					CompoundNBT tableNBT = NBTUtils.readSNBT(path);

					if (tableNBT != null)
					{
						RewardTable table = new RewardTable(this);
						table.id = readID(tableNBT.getInt("id"));
						map.put(table.id, table);
						dataCache.put(table.id, tableNBT);
						rewardTables.add(table);
					}
				});
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		for (QuestObjectBase object : map.values())
		{
			CompoundNBT data = dataCache.get(object.id);

			if (data != null)
			{
				object.readData(data);
			}
		}

		chapters.sort(Comparator.comparingInt(o -> o.orderIndex));
		updateChapterIndices();

		for (Chapter chapter : chapters)
		{
			for (Quest quest : chapter.quests)
			{
				quest.verifyDependencies(true);
			}
		}

		for (QuestObjectBase object : getAllObjects())
		{
			if (object instanceof CustomTask)
			{
				MinecraftForge.EVENT_BUS.post(new CustomTaskEvent((CustomTask) object));
			}
		}
	}

	public void save()
	{
	}

	public boolean updateChapterIndices()
	{
		boolean changed = false;

		for (int i = 0; i < chapters.size(); i++)
		{
			Chapter c = chapters.get(i);

			if (c.orderIndex != i + 1)
			{
				c.orderIndex = i + 1;
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public final void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		NetUtils.write(buffer, emergencyItems, FTBQuestsNetHandler::writeItemType);
		buffer.writeVarInt(emergencyItemsCooldown);
		buffer.writeBoolean(defaultRewardTeam);
		buffer.writeBoolean(defaultTeamConsumeItems);
		RewardAutoClaim.NAME_MAP_NO_DEFAULT.write(buffer, defaultRewardAutoClaim);
		buffer.writeString(defaultQuestShape, Short.MAX_VALUE);
		buffer.writeBoolean(defaultQuestDisableJEI);
		buffer.writeBoolean(dropLootCrates);
		lootCrateNoDrop.writeNetData(buffer);
		buffer.writeBoolean(disableGui);
		buffer.writeDouble(gridScale);
	}

	@Override
	public final void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		NetUtils.read(buffer, emergencyItems, FTBQuestsNetHandler::readItemType);
		emergencyItemsCooldown = buffer.readVarInt();
		defaultRewardTeam = buffer.readBoolean();
		defaultTeamConsumeItems = buffer.readBoolean();
		defaultRewardAutoClaim = RewardAutoClaim.NAME_MAP_NO_DEFAULT.read(buffer);
		defaultQuestShape = buffer.readString(Short.MAX_VALUE);
		defaultQuestDisableJEI = buffer.readBoolean();
		dropLootCrates = buffer.readBoolean();
		lootCrateNoDrop.readNetData(buffer);
		disableGui = buffer.readBoolean();
		gridScale = buffer.readDouble();
	}

	public final void writeNetDataFull(PacketBuffer buffer, UUID self)
	{
		int pos = buffer.writerIndex();
		writeNetData(buffer);

		buffer.writeVarInt(rewardTables.size());

		for (RewardTable table : rewardTables)
		{
			buffer.writeVarInt(table.id);
		}

		buffer.writeVarInt(chapters.size());

		ForgeRegistry<TaskType> taskTypes = TaskType.getRegistry();
		ForgeRegistry<RewardType> rewardTypes = RewardType.getRegistry();

		for (Chapter chapter : chapters)
		{
			buffer.writeVarInt(chapter.id);
			buffer.writeVarInt(chapter.quests.size());

			for (Quest quest : chapter.quests)
			{
				buffer.writeVarInt(quest.id);
				buffer.writeVarInt(quest.tasks.size());

				for (Task task : quest.tasks)
				{
					buffer.writeVarInt(taskTypes.getID(task.getType()));
					buffer.writeVarInt(task.id);
				}

				buffer.writeVarInt(quest.rewards.size());

				for (Reward reward : quest.rewards)
				{
					buffer.writeVarInt(rewardTypes.getID(reward.getType()));
					buffer.writeVarInt(reward.id);
				}
			}
		}

		for (RewardTable table : rewardTables)
		{
			table.writeNetData(buffer);
		}

		for (Chapter chapter : chapters)
		{
			chapter.writeNetData(buffer);

			for (Quest quest : chapter.quests)
			{
				quest.writeNetData(buffer);

				for (Task task : quest.tasks)
				{
					task.writeNetData(buffer);
				}

				for (Reward reward : quest.rewards)
				{
					reward.writeNetData(buffer);
				}
			}
		}

		PlayerData selfPlayerData = getData(self);
		buffer.writeVarInt(playerDataMap.size());

		for (PlayerData data : playerDataMap.values())
		{
			NetUtils.writeUUID(buffer, data.uuid);
			data.write(buffer, data == selfPlayerData);
		}

		FTBQuests.LOGGER.debug("Wrote " + (buffer.writerIndex() - pos) + " bytes");
	}

	public final void readNetDataFull(PacketBuffer buffer, UUID self)
	{
		int pos = buffer.readerIndex();
		readNetData(buffer);

		chapters.clear();
		rewardTables.clear();

		int rtl = buffer.readVarInt();

		for (int i = 0; i < rtl; i++)
		{
			RewardTable table = new RewardTable(this);
			table.id = buffer.readVarInt();
			rewardTables.add(table);
		}

		ForgeRegistry<TaskType> taskTypes = TaskType.getRegistry();
		ForgeRegistry<RewardType> rewardTypes = RewardType.getRegistry();

		int c = buffer.readVarInt();

		for (int i = 0; i < c; i++)
		{
			Chapter chapter = new Chapter(this);
			chapter.id = buffer.readVarInt();
			chapters.add(chapter);

			int q = buffer.readVarInt();

			for (int j = 0; j < q; j++)
			{
				Quest quest = new Quest(chapter);
				quest.id = buffer.readVarInt();
				chapter.quests.add(quest);

				int t = buffer.readVarInt();

				for (int k = 0; k < t; k++)
				{
					TaskType type = taskTypes.getValue(buffer.readVarInt());
					Task task = type.provider.create(quest);
					task.id = buffer.readVarInt();
					quest.tasks.add(task);
				}

				int r = buffer.readVarInt();

				for (int k = 0; k < r; k++)
				{
					RewardType type = rewardTypes.getValue(buffer.readVarInt());
					Reward reward = type.provider.create(quest);
					reward.id = buffer.readVarInt();
					quest.rewards.add(reward);
				}
			}
		}

		refreshIDMap();

		for (RewardTable table : rewardTables)
		{
			table.readNetData(buffer);
		}

		for (Chapter chapter : chapters)
		{
			chapter.readNetData(buffer);

			for (Quest quest : chapter.quests)
			{
				quest.readNetData(buffer);

				for (Task task : quest.tasks)
				{
					task.readNetData(buffer);
				}

				for (Reward reward : quest.rewards)
				{
					reward.readNetData(buffer);
				}
			}
		}

		for (QuestObjectBase object : getAllObjects())
		{
			if (object instanceof CustomTask)
			{
				MinecraftForge.EVENT_BUS.post(new CustomTaskEvent((CustomTask) object));
			}
		}

		int pds = buffer.readVarInt();

		for (int i = 0; i < pds; i++)
		{
			PlayerData data = new PlayerData(this, NetUtils.readUUID(buffer));
			addData(data, true);
			data.read(buffer, data.uuid.equals(self));
		}

		FTBQuests.LOGGER.debug("Read " + (buffer.readerIndex() - pos) + " bytes");
	}

	@Override
	public int getParentID()
	{
		return 0;
	}

	@Nullable
	public PlayerData getNullablePlayerData(UUID id)
	{
		return playerDataMap.get(id);
	}

	public PlayerData getData(UUID id)
	{
		return playerDataMap.computeIfAbsent(id, i -> new PlayerData(this, i));
	}

	public PlayerData getData(Entity player)
	{
		return getData(player.getUniqueID());
	}

	public Collection<PlayerData> getAllData()
	{
		return playerDataMap.values();
	}

	public abstract void deleteObject(int id);

	@Override
	public Icon getAltIcon()
	{
		return ThemeProperties.MODPACK_ICON.get(this);
	}

	@Override
	public IFormattableTextComponent getAltTitle()
	{
		return new TranslationTextComponent("ftbquests.file");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
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
	public void clearCachedData()
	{
		super.clearCachedData();

		for (Chapter chapter : chapters)
		{
			chapter.clearCachedData();
		}

		clearCachedProgress();

		MinecraftForge.EVENT_BUS.post(new ClearFileCacheEvent(this));
	}

	public void clearCachedProgress()
	{
		for (PlayerData data : getAllData())
		{
			data.clearCache();
		}
	}

	public int newID()
	{
		return readID(0);
	}

	public int readID(int id)
	{
		while (id == 0 || id == 1 || map.get(id) != null)
		{
			lastID++;
			id = lastID;
		}

		return id;
	}

	public int getID(@Nullable Object o)
	{
		if (o == null)
		{
			return 0;
		}
		else if (o instanceof Number)
		{
			return ((Number) o).intValue();
		}

		String id = o.toString();

		if (id.isEmpty())
		{
			return 0;
		}
		else if (id.charAt(0) == '*')
		{
			return 1;
		}

		try
		{
			return Long.valueOf(id.charAt(0) == '#' ? id.substring(1) : id).intValue();
		}
		catch (Exception ex)
		{
			for (QuestObjectBase b : map.values())
			{
				if (b.hasTag(id))
				{
					return b.id;
				}
			}

			return 0;
		}
	}

	@Nullable
	public LootCrate getRandomLootCrate(Entity entity, Random random)
	{
		int totalWeight = lootCrateNoDrop.getWeight(entity);

		for (RewardTable table : rewardTables)
		{
			if (table.lootCrate != null)
			{
				totalWeight += table.lootCrate.drops.getWeight(entity);
			}
		}

		if (totalWeight <= 0)
		{
			return null;
		}

		int number = random.nextInt(totalWeight) + 1;
		int currentWeight = lootCrateNoDrop.getWeight(entity);

		if (currentWeight < number)
		{
			for (RewardTable table : rewardTables)
			{
				if (table.lootCrate != null)
				{
					currentWeight += table.lootCrate.drops.getWeight(entity);

					if (currentWeight >= number)
					{
						return table.lootCrate;
					}
				}
			}
		}

		return null;
	}

	@Override
	public final int refreshJEI()
	{
		return FTBQuestsJEIHelper.QUESTS | FTBQuestsJEIHelper.LOOTCRATES;
	}

	public final Collection<QuestObjectBase> getAllObjects()
	{
		return map.values();
	}

	@Override
	public boolean isVisible(PlayerData data)
	{
		for (Chapter chapter : chapters)
		{
			if (chapter.isVisible(data))
			{
				return true;
			}
		}

		return false;
	}

	public List<Chapter> getVisibleChapters(PlayerData data, boolean excludeEmpty)
	{
		List<Chapter> list = new ArrayList<>();

		for (Chapter chapter : chapters)
		{
			if ((!excludeEmpty || !chapter.quests.isEmpty()) && chapter.isVisible(data))
			{
				list.add(chapter);
			}
		}

		return list;
	}

	public <T extends QuestObjectBase> List<T> collect(Class<T> clazz, Predicate<QuestObjectBase> filter)
	{
		List<T> list = new ArrayList<>();

		for (QuestObjectBase base : getAllObjects())
		{
			if (filter.test(base))
			{
				list.add((T) base);
			}
		}

		if (list.isEmpty())
		{
			return Collections.emptyList();
		}
		else if (list.size() == 1)
		{
			return Collections.singletonList(list.get(0));
		}

		return list;
	}

	public <T extends QuestObjectBase> List<T> collect(Class<T> clazz)
	{
		return collect(clazz, o -> clazz.isAssignableFrom(o.getClass()));
	}

	public String getDefaultQuestShape()
	{
		return defaultQuestShape;
	}

	public void addData(PlayerData data, boolean strong)
	{
		playerDataMap.put(data.uuid, data);

		for (Chapter chapter : chapters)
		{
			for (Quest quest : chapter.quests)
			{
				for (Task task : quest.tasks)
				{
					data.createTaskData(task, strong);
				}
			}
		}
	}
}