package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.events.ClearFileCacheEvent;
import com.feed_the_beast.ftbquests.events.CustomTaskEvent;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.net.MessageDisplayCompletionToast;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.feed_the_beast.ftbquests.quest.loot.EntityWeight;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardAutoClaim;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.feed_the_beast.ftbquests.util.NBTUtils;
import com.feed_the_beast.ftbquests.util.NetUtils;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigItemStack;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.IconAnimation;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistry;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public abstract class QuestFile extends QuestObject
{
	public static final int VERSION = 7;

	private int lastID;
	public final List<Chapter> chapters;
	public final List<RewardTable> rewardTables;
	private final Map<UUID, PlayerData> playerDataMap;

	private final Int2ObjectOpenHashMap<QuestObjectBase> map;

	public final List<ItemStack> emergencyItems;
	public int emergencyItemsCooldown;
	public int fileVersion;
	public boolean defaultRewardTeam;
	public boolean defaultTeamConsumeItems;
	public RewardAutoClaim defaultRewardAutoClaim;
	public QuestShape defaultQuestShape;
	public boolean defaultQuestDisableJEI;
	public boolean dropLootCrates;
	public final EntityWeight lootCrateNoDrop;
	public boolean disableGui;
	public String folderName;

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
		emergencyItems.add(new ItemStack(Items.APPLE));
		emergencyItemsCooldown = 300;

		defaultRewardTeam = false;
		defaultTeamConsumeItems = false;
		defaultRewardAutoClaim = RewardAutoClaim.DISABLED;
		defaultQuestShape = QuestShape.CIRCLE;
		defaultQuestDisableJEI = false;
		dropLootCrates = false;
		lootCrateNoDrop = new EntityWeight();
		lootCrateNoDrop.passive = 4000;
		lootCrateNoDrop.monster = 600;
		lootCrateNoDrop.boss = 0;
		disableGui = false;
		folderName = "";
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

	public File getFolder()
	{
		throw new IllegalStateException("This quest file doesn't have a folder!");
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
		nbt.putString("default_quest_shape", defaultQuestShape.id);
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

		CompoundNBT nbt1 = new CompoundNBT();
		lootCrateNoDrop.writeData(nbt1);
		nbt.put("loot_crate_no_drop", nbt1);
		nbt.putBoolean("disable_gui", disableGui);
	}

	@Override
	public final void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		defaultRewardTeam = nbt.getBoolean("default_reward_team");
		defaultTeamConsumeItems = nbt.getBoolean("default_consume_items");
		defaultRewardAutoClaim = RewardAutoClaim.NAME_MAP_NO_DEFAULT.get(nbt.getString("default_autoclaim_rewards"));
		defaultQuestShape = QuestShape.NAME_MAP.get(nbt.getString("default_quest_shape"));
		defaultQuestDisableJEI = nbt.getBoolean("default_quest_disable_jei");
		emergencyItems.clear();

		ListNBT list = nbt.getList("emergency_items", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.size(); i++)
		{
			ItemStack stack = ItemStack.read(list.getCompound(i));

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
	}

	private CompoundNBT createIndex(List<? extends QuestObjectBase> list)
	{
		int[] index = new int[list.size()];

		for (int i = 0; i < index.length; i++)
		{
			index[i] = list.get(i).id;
		}

		CompoundNBT nbt = new CompoundNBT();
		nbt.putIntArray("index", index);
		return nbt;
	}

	public int[] readIndex(File file)
	{
		CompoundNBT nbt = NBTUtils.readNBT(file);
		return nbt == null ? new int[0] : nbt.getIntArray("index");
	}

	public final void writeDataFull(File folder)
	{
		CompoundNBT out = new CompoundNBT();
		out.putInt("version", VERSION);
		out.putInt("last_id", lastID);
		writeData(out);
		NBTUtils.writeNBTSafe(new File(folder, "file.nbt"), out);

		NBTUtils.writeNBTSafe(new File(folder, "chapters/index.nbt"), createIndex(chapters));
		NBTUtils.writeNBTSafe(new File(folder, "reward_tables/index.nbt"), createIndex(rewardTables));

		for (Chapter chapter : chapters)
		{
			out = new CompoundNBT();
			chapter.writeData(out);
			String chapterPath = "chapters/" + getCodeString(chapter) + "/";
			NBTUtils.writeNBTSafe(new File(folder, chapterPath + "chapter.nbt"), out);

			if (!chapter.quests.isEmpty())
			{
				for (Quest quest : chapter.quests)
				{
					if (quest.invalid)
					{
						continue;
					}

					out = new CompoundNBT();
					quest.writeData(out);

					if (!quest.tasks.isEmpty())
					{
						ListNBT t = new ListNBT();

						for (Task task : quest.tasks)
						{
							TaskType type = task.getType();
							CompoundNBT nbt3 = new CompoundNBT();
							task.writeData(nbt3);
							nbt3.putInt("uid", task.id);
							nbt3.putString("type", type.getTypeForNBT());
							t.add(nbt3);
						}

						if (!t.isEmpty())
						{
							out.put("tasks", t);
						}
					}

					if (!quest.rewards.isEmpty())
					{
						ListNBT r = new ListNBT();

						for (Reward reward : quest.rewards)
						{
							RewardType type = reward.getType();
							CompoundNBT nbt3 = new CompoundNBT();
							reward.writeData(nbt3);
							nbt3.putInt("uid", reward.id);
							nbt3.putString("type", type.getTypeForNBT());
							r.add(nbt3);
						}

						if (!r.isEmpty())
						{
							out.put("rewards", r);
						}
					}

					NBTUtils.writeNBTSafe(new File(folder, chapterPath + getCodeString(quest) + ".nbt"), out);
				}
			}
		}

		for (RewardTable table : rewardTables)
		{
			out = new CompoundNBT();
			table.writeData(out);
			NBTUtils.writeNBTSafe(new File(folder, "reward_tables/" + getCodeString(table) + ".nbt"), out);
		}
	}

	public final void readDataFull(File folder)
	{
		CompoundNBT nbt = NBTUtils.readNBT(new File(folder, "file.nbt"));

		if (nbt != null)
		{
			fileVersion = nbt.getInt("version");
			lastID = nbt.getInt("last_id");
			readData(nbt);
		}

		chapters.clear();
		rewardTables.clear();

		Int2ObjectOpenHashMap<CompoundNBT> questFileCache = new Int2ObjectOpenHashMap<>();

		for (int i : readIndex(new File(folder, "chapters/index.nbt")))
		{
			Chapter chapter = new Chapter(this);
			chapter.id = i;
			chapters.add(chapter);

			File[] files = new File(folder, "chapters/" + getCodeString(chapter)).listFiles();

			if (files != null && files.length > 0)
			{
				for (File f : files)
				{
					if (!f.getName().equals("chapter.nbt"))
					{
						try
						{
							Quest quest = new Quest(chapter);
							quest.id = Long.decode("#" + f.getName().replace(".nbt", "")).intValue();

							nbt = NBTUtils.readNBT(quest.getFile());

							if (nbt != null)
							{
								questFileCache.put(quest.id, nbt);
								ListNBT t = nbt.getList("tasks", Constants.NBT.TAG_COMPOUND);

								for (int k = 0; k < t.size(); k++)
								{
									CompoundNBT tt = t.getCompound(k);
									Task task = TaskType.createTask(quest, tt.getString("type"));

									if (task != null)
									{
										task.id = tt.getInt("uid");
										quest.tasks.add(task);
									}
								}

								ListNBT r = nbt.getList("rewards", Constants.NBT.TAG_COMPOUND);

								for (int k = 0; k < r.size(); k++)
								{
									CompoundNBT rt = r.getCompound(k);
									Reward reward = RewardType.createReward(quest, rt.getString("type"));

									if (reward != null)
									{
										reward.id = rt.getInt("uid");
										quest.rewards.add(reward);
									}
								}

								chapter.quests.add(quest);
							}
						}
						catch (Exception ex)
						{
							FTBQuests.LOGGER.warn("Failed to read quest ID " + f.getName());
						}
					}
				}
			}
		}

		for (int i : readIndex(new File(folder, "reward_tables/index.nbt")))
		{
			RewardTable table = new RewardTable(this);
			table.id = i;
			rewardTables.add(table);
		}

		refreshIDMap();

		for (Chapter chapter : chapters)
		{
			nbt = NBTUtils.readNBT(new File(folder, "chapters/" + getCodeString(chapter) + "/chapter.nbt"));

			if (nbt != null)
			{
				chapter.readData(nbt);
			}

			for (Quest quest : chapter.quests)
			{
				nbt = questFileCache.get(quest.id);

				if (nbt == null)
				{
					continue;
				}

				quest.readData(nbt);

				ListNBT t = nbt.getList("tasks", Constants.NBT.TAG_COMPOUND);

				for (int k = 0; k < t.size(); k++)
				{
					CompoundNBT tt = t.getCompound(k);
					Task task = getTask(tt.getInt("uid"));

					if (task != null)
					{
						task.readData(tt);
					}
				}

				ListNBT r = nbt.getList("rewards", Constants.NBT.TAG_COMPOUND);

				for (int k = 0; k < r.size(); k++)
				{
					CompoundNBT rt = r.getCompound(k);
					Reward reward = getReward(rt.getInt("uid"));

					if (reward != null)
					{
						reward.readData(rt);
					}
				}
			}
		}

		for (RewardTable table : rewardTables)
		{
			nbt = NBTUtils.readNBT(new File(folder, "reward_tables/" + getCodeString(table) + ".nbt"));

			if (nbt != null)
			{
				table.readData(nbt);
			}
		}

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

	@Override
	public final void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		NetUtils.write(buffer, emergencyItems, PacketBuffer::writeItemStack);
		buffer.writeVarInt(emergencyItemsCooldown);
		buffer.writeBoolean(defaultRewardTeam);
		buffer.writeBoolean(defaultTeamConsumeItems);
		RewardAutoClaim.NAME_MAP_NO_DEFAULT.write(buffer, defaultRewardAutoClaim);
		QuestShape.NAME_MAP.write(buffer, defaultQuestShape);
		buffer.writeBoolean(defaultQuestDisableJEI);
		buffer.writeBoolean(dropLootCrates);
		lootCrateNoDrop.writeNetData(buffer);
		buffer.writeBoolean(disableGui);
		buffer.writeString(folderName);
	}

	@Override
	public final void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		NetUtils.read(buffer, emergencyItems, PacketBuffer::readItemStack);
		emergencyItemsCooldown = buffer.readVarInt();
		defaultRewardTeam = buffer.readBoolean();
		defaultTeamConsumeItems = buffer.readBoolean();
		defaultRewardAutoClaim = RewardAutoClaim.NAME_MAP_NO_DEFAULT.read(buffer);
		defaultQuestShape = QuestShape.NAME_MAP.read(buffer);
		defaultQuestDisableJEI = buffer.readBoolean();
		dropLootCrates = buffer.readBoolean();
		lootCrateNoDrop.readNetData(buffer);
		disableGui = buffer.readBoolean();
		folderName = buffer.readString();
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
		NetUtils.write(buffer, playerDataMap, NetUtils::writeUUID, (b, d) -> d.write(b, d == selfPlayerData));

		//FIXME: if (FTBLibConfig.debugging.print_more_info)
		{
			FTBQuests.LOGGER.info("Wrote " + (buffer.writerIndex() - pos) + " bytes");
		}
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

		NetUtils.read(buffer, playerDataMap, NetUtils::readUUID, (key, b) -> {
			PlayerData data = new PlayerData(this, key, "");

			for (Chapter chapter : chapters)
			{
				for (Quest quest : chapter.quests)
				{
					for (Task task : quest.tasks)
					{
						data.createTaskData(task);
					}
				}
			}

			data.read(b, true);
			return data;
		});

		//FIXME: if (FTBLibConfig.debugging.print_more_info)
		{
			FTBQuests.LOGGER.info("Read " + (buffer.readerIndex() - pos) + " bytes");
		}
	}

	@Override
	public int getParentID()
	{
		return 0;
	}

	public PlayerData getData(UUID id)
	{
		return Objects.requireNonNull(playerDataMap.get(id));
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
		List<Icon> list = new ArrayList<>();

		for (Chapter chapter : chapters)
		{
			list.add(chapter.getIcon());
		}

		return IconAnimation.fromList(list, false);
	}

	@Override
	public String getAltTitle()
	{
		return I18n.format("ftbquests.file");
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

		ConfigGroup defaultsGroup = config.getGroup("defaults");
		defaultsGroup.addBool("reward_team", defaultRewardTeam, v -> defaultRewardTeam = v, false);
		defaultsGroup.addBool("consume_items", defaultTeamConsumeItems, v -> defaultTeamConsumeItems = v, false);
		defaultsGroup.addEnum("autoclaim_rewards", defaultRewardAutoClaim, v -> defaultRewardAutoClaim = v, RewardAutoClaim.NAME_MAP_NO_DEFAULT);
		defaultsGroup.addEnum("quest_shape", defaultQuestShape, v -> defaultQuestShape = v, QuestShape.NAME_MAP.withDefault(QuestShape.CIRCLE));
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
			return Long.valueOf(id.charAt(0) == '#' ? id.substring(1) : id, 16).intValue();
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

	public QuestShape getDefaultQuestShape()
	{
		return defaultQuestShape == QuestShape.DEFAULT ? QuestShape.CIRCLE : defaultQuestShape;
	}

	public void addData(PlayerData data)
	{
		playerDataMap.put(data.uuid, data);
	}

	public void onLoggedIn(ServerPlayerEntity player)
	{
		UUID id = player.getUniqueID();
		PlayerData data = playerDataMap.get(id);

		if (data != null)
		{
			data.name = player.getGameProfile().getName();
			data.markDirty();
			//FIXME: Send update to other players
		}
		else
		{
			data = new PlayerData(this, id, player.getGameProfile().getName());
			playerDataMap.put(id, data);
		}

		new MessageSyncQuests(id, this).sendTo(player);
	}
}