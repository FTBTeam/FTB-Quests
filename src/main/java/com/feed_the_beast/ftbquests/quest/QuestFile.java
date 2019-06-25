package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.FTBLibConfig;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigTimer;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.math.MathUtils;
import com.feed_the_beast.ftblib.lib.math.Ticks;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.events.ClearFileCacheEvent;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.net.MessageDisplayCompletionToast;
import com.feed_the_beast.ftbquests.quest.loot.EntityWeight;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import com.latmod.mods.itemfilters.item.ItemMissing;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistry;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public abstract class QuestFile extends QuestObject
{
	public static final int VERSION = 5;

	public final List<QuestChapter> chapters;
	public final List<RewardTable> rewardTables;

	private final Int2ObjectOpenHashMap<QuestObjectBase> map;
	public final RewardTable dummyTable;

	public final List<ItemStack> emergencyItems;
	public Ticks emergencyItemsCooldown;
	public int fileVersion;
	public boolean defaultRewardTeam;
	public boolean defaultTeamConsumeItems;
	public boolean defaultRewardAutoclaim;
	public EnumQuestShape defaultShape;
	public boolean dropLootCrates;
	public final EntityWeight lootCrateNoDrop;
	public boolean disableGui;

	public QuestFile()
	{
		id = 1;
		fileVersion = 0;
		chapters = new ArrayList<>();
		rewardTables = new ArrayList<>();

		map = new Int2ObjectOpenHashMap<>();
		dummyTable = new RewardTable(this);

		emergencyItems = new ArrayList<>();
		emergencyItems.add(new ItemStack(Items.APPLE));
		emergencyItemsCooldown = Ticks.MINUTE.x(5);

		defaultRewardTeam = false;
		defaultTeamConsumeItems = false;
		defaultRewardAutoclaim = false;
		defaultShape = EnumQuestShape.CIRCLE;
		dropLootCrates = false;
		lootCrateNoDrop = new EntityWeight();
		lootCrateNoDrop.passive = 4000;
		lootCrateNoDrop.monster = 600;
		lootCrateNoDrop.boss = 0;
		disableGui = false;
	}

	public abstract boolean isClient();

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

	@Override
	public int getRelativeProgressFromChildren(QuestData data)
	{
		int progress = 0;

		for (QuestChapter chapter : chapters)
		{
			progress += chapter.getRelativeProgress(data);
		}

		return getRelativeProgressFromChildren(progress, chapters.size());
	}

	@Override
	public void onCompleted(QuestData data, List<EntityPlayerMP> notifyPlayers)
	{
		super.onCompleted(data, notifyPlayers);
		new ObjectCompletedEvent.FileEvent(data, this).post();

		for (EntityPlayerMP player : notifyPlayers)
		{
			new MessageDisplayCompletionToast(id).sendTo(player);
		}
	}

	@Override
	public void changeProgress(QuestData data, EnumChangeProgress type)
	{
		for (QuestChapter chapter : chapters)
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
		for (QuestChapter chapter : chapters)
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

				for (QuestChapter chapter : chapters)
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
	public QuestChapter getChapter(int id)
	{
		QuestObjectBase object = getBase(id);
		return object instanceof QuestChapter ? (QuestChapter) object : null;
	}

	@Nullable
	public Quest getQuest(int id)
	{
		QuestObjectBase object = getBase(id);
		return object instanceof Quest ? (Quest) object : null;
	}

	@Nullable
	public QuestTask getTask(int id)
	{
		QuestObjectBase object = getBase(id);
		return object instanceof QuestTask ? (QuestTask) object : null;
	}

	@Nullable
	public QuestReward getReward(int id)
	{
		QuestObjectBase object = getBase(id);
		return object instanceof QuestReward ? (QuestReward) object : null;
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

		for (QuestChapter chapter : chapters)
		{
			map.put(chapter.id, chapter);

			for (Quest quest : chapter.quests)
			{
				map.put(quest.id, quest);

				for (QuestTask task : quest.tasks)
				{
					map.put(task.id, task);
				}

				for (QuestReward reward : quest.rewards)
				{
					map.put(reward.id, reward);
				}
			}
		}

		clearCachedData();
	}

	public QuestObjectBase create(QuestObjectType type, int parent, NBTTagCompound extra)
	{
		switch (type)
		{
			case CHAPTER:
				return new QuestChapter(this);
			case QUEST:
			{
				QuestChapter chapter = getChapter(parent);

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
					QuestTask task = QuestTaskType.createTask(quest, extra.getString("type"));

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
					QuestReward reward = QuestRewardType.createReward(quest, extra.getString("type"));

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
	public final void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setInteger("version", VERSION);
		nbt.setBoolean("default_reward_team", defaultRewardTeam);
		nbt.setBoolean("default_consume_items", defaultTeamConsumeItems);
		nbt.setBoolean("default_autoclaim_rewards", defaultRewardAutoclaim);
		nbt.setString("default_quest_shape", defaultShape.getID());

		if (!emergencyItems.isEmpty())
		{
			NBTTagList list = new NBTTagList();

			for (ItemStack stack : emergencyItems)
			{
				list.appendTag(ItemMissing.write(stack, true));
			}

			nbt.setTag("emergency_items", list);
		}

		nbt.setString("emergency_items_cooldown", emergencyItemsCooldown.toString());
		nbt.setBoolean("drop_loot_crates", dropLootCrates);

		NBTTagCompound nbt1 = new NBTTagCompound();
		lootCrateNoDrop.writeData(nbt1);
		nbt.setTag("loot_crate_no_drop", nbt1);
		nbt.setBoolean("disable_gui", disableGui);
	}

	@Override
	public final void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		defaultRewardTeam = nbt.getBoolean("default_reward_team");
		defaultTeamConsumeItems = nbt.getBoolean("default_consume_items");
		defaultRewardAutoclaim = nbt.getBoolean("default_autoclaim_rewards");
		defaultShape = EnumQuestShape.NAME_MAP.get(nbt.getString("default_quest_shape"));
		emergencyItems.clear();

		NBTTagList list = nbt.getTagList("emergency_items", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); i++)
		{
			ItemStack stack = ItemMissing.read(list.getCompoundTagAt(i));

			if (!stack.isEmpty())
			{
				emergencyItems.add(stack);
			}
		}

		Ticks t = Ticks.get(nbt.getString("emergency_items_cooldown"));
		emergencyItemsCooldown = t.hasTicks() ? t : Ticks.MINUTE.x(5);
		dropLootCrates = nbt.getBoolean("drop_loot_crates");

		if (nbt.hasKey("loot_crate_no_drop"))
		{
			lootCrateNoDrop.readData(nbt.getCompoundTag("loot_crate_no_drop"));
		}

		disableGui = nbt.getBoolean("disable_gui");
	}

	private NBTTagCompound createIndex(List<? extends QuestObjectBase> list)
	{
		int[] index = new int[list.size()];

		for (int i = 0; i < index.length; i++)
		{
			index[i] = list.get(i).id;
		}

		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setIntArray("index", index);
		return nbt;
	}

	public int[] readIndex(File file)
	{
		NBTTagCompound nbt = NBTUtils.readNBT(file);
		return nbt == null ? new int[0] : nbt.getIntArray("index");
	}

	public final void writeDataFull(File folder)
	{
		NBTTagCompound out = new NBTTagCompound();
		writeData(out);
		NBTUtils.writeNBTSafe(new File(folder, "file.nbt"), out);

		NBTUtils.writeNBTSafe(new File(folder, "chapters/index.nbt"), createIndex(chapters));
		NBTUtils.writeNBTSafe(new File(folder, "reward_tables/index.nbt"), createIndex(rewardTables));

		for (QuestChapter chapter : chapters)
		{
			out = new NBTTagCompound();
			chapter.writeData(out);
			String chapterPath = "chapters/" + chapter.getCodeString() + "/";
			NBTUtils.writeNBTSafe(new File(folder, chapterPath + "chapter.nbt"), out);

			if (!chapter.quests.isEmpty())
			{
				for (Quest quest : chapter.quests)
				{
					if (quest.invalid)
					{
						continue;
					}

					out = new NBTTagCompound();
					quest.writeData(out);

					if (!quest.tasks.isEmpty())
					{
						NBTTagList t = new NBTTagList();

						for (QuestTask task : quest.tasks)
						{
							QuestTaskType type = task.getType();
							NBTTagCompound nbt3 = new NBTTagCompound();
							task.writeData(nbt3);
							nbt3.setInteger("uid", task.id);
							nbt3.setString("type", type.getTypeForNBT());
							t.appendTag(nbt3);
						}

						if (!t.isEmpty())
						{
							out.setTag("tasks", t);
						}
					}

					if (!quest.rewards.isEmpty())
					{
						NBTTagList r = new NBTTagList();

						for (QuestReward reward : quest.rewards)
						{
							QuestRewardType type = reward.getType();
							NBTTagCompound nbt3 = new NBTTagCompound();
							reward.writeData(nbt3);
							nbt3.setInteger("uid", reward.id);
							nbt3.setString("type", type.getTypeForNBT());
							r.appendTag(nbt3);
						}

						if (!r.isEmpty())
						{
							out.setTag("rewards", r);
						}
					}

					NBTUtils.writeNBTSafe(new File(folder, chapterPath + quest.getCodeString() + ".nbt"), out);
				}
			}
		}

		for (RewardTable table : rewardTables)
		{
			out = new NBTTagCompound();
			table.writeData(out);
			NBTUtils.writeNBTSafe(new File(folder, "reward_tables/" + table.getCodeString() + ".nbt"), out);
		}
	}

	public final void readDataOld(NBTTagCompound nbt)
	{
		fileVersion = nbt.getInteger("version");
		readData(nbt);

		chapters.clear();
		rewardTables.clear();

		Int2ObjectOpenHashMap<NBTTagCompound> objectDataCache = new Int2ObjectOpenHashMap<>();

		NBTTagList rtl = nbt.getTagList("reward_tables", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < rtl.tagCount(); i++)
		{
			RewardTable table = new RewardTable(this);
			NBTTagCompound nbt1 = rtl.getCompoundTagAt(i);
			table.id = nbt1.getInteger("uid");
			rewardTables.add(table);
			objectDataCache.put(table.id, nbt1);
		}

		NBTTagList c = nbt.getTagList("chapters", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < c.tagCount(); i++)
		{
			QuestChapter chapter = new QuestChapter(this);
			NBTTagCompound ct = c.getCompoundTagAt(i);
			chapter.id = ct.getInteger("uid");
			chapters.add(chapter);
			objectDataCache.put(chapter.id, ct);

			NBTTagList q = ct.getTagList("quests", Constants.NBT.TAG_COMPOUND);

			for (int j = 0; j < q.tagCount(); j++)
			{
				Quest quest = new Quest(chapter);
				NBTTagCompound qt = q.getCompoundTagAt(j);
				quest.id = qt.getInteger("uid");
				chapter.quests.add(quest);
				objectDataCache.put(quest.id, qt);

				NBTTagList t = qt.getTagList("tasks", Constants.NBT.TAG_COMPOUND);

				if (t.isEmpty())
				{
					NBTBase tt = qt.getTag("task");

					if (tt != null)
					{
						t.appendTag(tt);
					}
				}

				for (int k = 0; k < t.tagCount(); k++)
				{
					NBTTagCompound tt = t.getCompoundTagAt(k);
					QuestTask task = QuestTaskType.createTask(quest, tt.getString("type"));

					if (task != null)
					{
						task.id = tt.getInteger("uid");
						objectDataCache.put(task.id, tt);
						quest.tasks.add(task);
					}
				}

				NBTTagList r = qt.getTagList("rewards", Constants.NBT.TAG_COMPOUND);

				if (r.isEmpty())
				{
					NBTBase rt = qt.getTag("reward");

					if (rt != null)
					{
						r.appendTag(rt);
					}
				}

				for (int k = 0; k < r.tagCount(); k++)
				{
					NBTTagCompound rt = r.getCompoundTagAt(k);
					QuestReward reward = QuestRewardType.createReward(quest, rt.getString("type"));

					if (reward != null)
					{
						reward.id = rt.getInteger("uid");
						objectDataCache.put(reward.id, rt);
						quest.rewards.add(reward);
					}
				}
			}
		}

		refreshIDMap();

		NBTTagCompound nbt1;

		for (QuestChapter chapter : chapters)
		{
			nbt1 = objectDataCache.get(chapter.id);

			if (nbt1 != null)
			{
				chapter.readData(nbt1);
			}

			for (Quest quest : chapter.quests)
			{
				nbt1 = objectDataCache.get(quest.id);

				if (nbt1 != null)
				{
					quest.readData(nbt1);
				}

				for (QuestTask task : quest.tasks)
				{
					nbt1 = objectDataCache.get(task.id);

					if (nbt1 != null)
					{
						task.readData(nbt1);
					}
				}

				for (QuestReward reward : quest.rewards)
				{
					nbt1 = objectDataCache.get(reward.id);

					if (nbt1 != null)
					{
						reward.readData(nbt1);
					}
				}
			}
		}

		for (RewardTable table : rewardTables)
		{
			nbt1 = objectDataCache.get(table.id);

			if (nbt1 != null)
			{
				table.readData(nbt1);
			}
		}

		for (QuestChapter chapter : chapters)
		{
			for (Quest quest : chapter.quests)
			{
				quest.verifyDependencies(true);
			}
		}
	}

	public final void readDataFull(File folder)
	{
		NBTTagCompound nbt = NBTUtils.readNBT(new File(folder, "file.nbt"));

		if (nbt != null)
		{
			fileVersion = nbt.getInteger("version");
			readData(nbt);
		}

		chapters.clear();
		rewardTables.clear();

		Int2ObjectOpenHashMap<NBTTagCompound> questFileCache = new Int2ObjectOpenHashMap<>();

		for (int i : readIndex(new File(folder, "chapters/index.nbt")))
		{
			QuestChapter chapter = new QuestChapter(this);
			chapter.id = i;
			chapters.add(chapter);

			File[] files = new File(folder, "chapters/" + chapter.getCodeString()).listFiles();

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

							nbt = NBTUtils.readNBT(new File(folder, "chapters/" + chapter.getCodeString() + "/" + quest.getCodeString() + ".nbt"));

							if (nbt != null)
							{
								questFileCache.put(quest.id, nbt);
								NBTTagList t = nbt.getTagList("tasks", Constants.NBT.TAG_COMPOUND);

								for (int k = 0; k < t.tagCount(); k++)
								{
									NBTTagCompound tt = t.getCompoundTagAt(k);
									QuestTask task = QuestTaskType.createTask(quest, tt.getString("type"));

									if (task != null)
									{
										task.id = tt.getInteger("uid");
										quest.tasks.add(task);
									}
								}

								NBTTagList r = nbt.getTagList("rewards", Constants.NBT.TAG_COMPOUND);

								for (int k = 0; k < r.tagCount(); k++)
								{
									NBTTagCompound rt = r.getCompoundTagAt(k);
									QuestReward reward = QuestRewardType.createReward(quest, rt.getString("type"));

									if (reward != null)
									{
										reward.id = rt.getInteger("uid");
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

		for (QuestChapter chapter : chapters)
		{
			nbt = NBTUtils.readNBT(new File(folder, "chapters/" + chapter.getCodeString() + "/chapter.nbt"));

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

				NBTTagList t = nbt.getTagList("tasks", Constants.NBT.TAG_COMPOUND);

				for (int k = 0; k < t.tagCount(); k++)
				{
					NBTTagCompound tt = t.getCompoundTagAt(k);
					QuestTask task = getTask(tt.getInteger("uid"));

					if (task != null)
					{
						task.readData(tt);
					}
				}

				NBTTagList r = nbt.getTagList("rewards", Constants.NBT.TAG_COMPOUND);

				for (int k = 0; k < r.tagCount(); k++)
				{
					NBTTagCompound rt = r.getCompoundTagAt(k);
					QuestReward reward = getReward(rt.getInteger("uid"));

					if (reward != null)
					{
						reward.readData(rt);
					}
				}
			}
		}

		for (RewardTable table : rewardTables)
		{
			nbt = NBTUtils.readNBT(new File(folder, "reward_tables/" + table.getCodeString() + ".nbt"));

			if (nbt != null)
			{
				table.readData(nbt);
			}
		}

		for (QuestChapter chapter : chapters)
		{
			for (Quest quest : chapter.quests)
			{
				quest.verifyDependencies(true);
			}
		}
	}

	@Override
	public final void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeCollection(emergencyItems, DataOut.ITEM_STACK);
		data.writeVarLong(emergencyItemsCooldown.ticks());
		data.writeBoolean(defaultRewardTeam);
		data.writeBoolean(defaultTeamConsumeItems);
		data.writeBoolean(defaultRewardAutoclaim);
		data.write(defaultShape, EnumQuestShape.NAME_MAP);

		data.writeBoolean(dropLootCrates);
		lootCrateNoDrop.writeNetData(data);
		data.writeBoolean(disableGui);
	}

	@Override
	public final void readNetData(DataIn data)
	{
		super.readNetData(data);
		data.readCollection(emergencyItems, DataIn.ITEM_STACK);
		emergencyItemsCooldown = Ticks.get(data.readVarLong());
		defaultRewardTeam = data.readBoolean();
		defaultTeamConsumeItems = data.readBoolean();
		defaultRewardAutoclaim = data.readBoolean();
		defaultShape = data.read(EnumQuestShape.NAME_MAP);
		dropLootCrates = data.readBoolean();
		lootCrateNoDrop.readNetData(data);
		disableGui = data.readBoolean();
	}

	public final void writeNetDataFull(DataOut data)
	{
		int pos = data.getPosition();
		writeNetData(data);

		data.writeVarInt(rewardTables.size());

		for (RewardTable table : rewardTables)
		{
			data.writeInt(table.id);
		}

		data.writeVarInt(chapters.size());

		ForgeRegistry<QuestTaskType> taskTypes = QuestTaskType.getRegistry();
		ForgeRegistry<QuestRewardType> rewardTypes = QuestRewardType.getRegistry();

		for (QuestChapter chapter : chapters)
		{
			data.writeInt(chapter.id);
			data.writeVarInt(chapter.quests.size());

			for (Quest quest : chapter.quests)
			{
				data.writeInt(quest.id);
				data.writeVarInt(quest.tasks.size());

				for (QuestTask task : quest.tasks)
				{
					data.writeVarInt(taskTypes.getID(task.getType()));
					data.writeInt(task.id);
				}

				data.writeVarInt(quest.rewards.size());

				for (QuestReward reward : quest.rewards)
				{
					data.writeVarInt(rewardTypes.getID(reward.getType()));
					data.writeInt(reward.id);
				}
			}
		}

		for (RewardTable table : rewardTables)
		{
			table.writeNetData(data);
		}

		for (QuestChapter chapter : chapters)
		{
			chapter.writeNetData(data);

			for (Quest quest : chapter.quests)
			{
				quest.writeNetData(data);

				for (QuestTask task : quest.tasks)
				{
					task.writeNetData(data);
				}

				for (QuestReward reward : quest.rewards)
				{
					reward.writeNetData(data);
				}
			}
		}

		if (FTBLibConfig.debugging.print_more_info)
		{
			FTBQuests.LOGGER.info("Wrote " + (data.getPosition() - pos) + " bytes");
		}
	}

	public final void readNetDataFull(DataIn data)
	{
		int pos = data.getPosition();
		readNetData(data);

		chapters.clear();
		rewardTables.clear();

		int rtl = data.readVarInt();

		for (int i = 0; i < rtl; i++)
		{
			RewardTable table = new RewardTable(this);
			table.id = data.readInt();
			rewardTables.add(table);
		}

		ForgeRegistry<QuestTaskType> taskTypes = QuestTaskType.getRegistry();
		ForgeRegistry<QuestRewardType> rewardTypes = QuestRewardType.getRegistry();

		int c = data.readVarInt();

		for (int i = 0; i < c; i++)
		{
			QuestChapter chapter = new QuestChapter(this);
			chapter.id = data.readInt();
			chapters.add(chapter);

			int q = data.readVarInt();

			for (int j = 0; j < q; j++)
			{
				Quest quest = new Quest(chapter);
				quest.id = data.readInt();
				chapter.quests.add(quest);

				int t = data.readVarInt();

				for (int k = 0; k < t; k++)
				{
					QuestTaskType type = taskTypes.getValue(data.readVarInt());
					QuestTask task = type.provider.create(quest);
					task.id = data.readInt();
					quest.tasks.add(task);
				}

				int r = data.readVarInt();

				for (int k = 0; k < r; k++)
				{
					QuestRewardType type = rewardTypes.getValue(data.readVarInt());
					QuestReward reward = type.provider.create(quest);
					reward.id = data.readInt();
					quest.rewards.add(reward);
				}
			}
		}

		refreshIDMap();

		for (RewardTable table : rewardTables)
		{
			table.readNetData(data);
		}

		for (QuestChapter chapter : chapters)
		{
			chapter.readNetData(data);

			for (Quest quest : chapter.quests)
			{
				quest.readNetData(data);

				for (QuestTask task : quest.tasks)
				{
					task.readNetData(data);
				}

				for (QuestReward reward : quest.rewards)
				{
					reward.readNetData(data);
				}
			}
		}

		if (FTBLibConfig.debugging.print_more_info)
		{
			FTBQuests.LOGGER.info("Read " + (data.getPosition() - pos) + " bytes");
		}
	}

	@Nullable
	public abstract QuestData getData(short team);

	@Nullable
	public abstract QuestData getData(String team);

	@Nullable
	public final QuestData getData(EntityPlayer player)
	{
		return getData(FTBLibAPI.getTeamID(player.getUniqueID()));
	}

	public abstract Collection<? extends QuestData> getAllData();

	public abstract void deleteObject(int id);

	@Override
	public Icon getAltIcon()
	{
		List<Icon> list = new ArrayList<>();

		for (QuestChapter chapter : chapters)
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
	public void getConfig(EntityPlayer player, ConfigGroup config)
	{
		super.getConfig(player, config);
		config.addList("emergency_items", emergencyItems, new ConfigItemStack(ItemStack.EMPTY), ConfigItemStack::new, ConfigItemStack::getStack);

		config.add("emergency_items_cooldown", new ConfigTimer(Ticks.NO_TICKS)
		{
			@Override
			public Ticks getTimer()
			{
				return emergencyItemsCooldown;
			}

			@Override
			public void setTimer(Ticks t)
			{
				emergencyItemsCooldown = t;
			}
		}, new ConfigTimer(Ticks.MINUTE.x(5)));

		config.addBool("drop_loot_crates", () -> dropLootCrates, v -> dropLootCrates = v, false);
		config.addBool("disable_gui", () -> disableGui, v -> disableGui = v, false);

		ConfigGroup defaultsGroup = config.getGroup("defaults");
		defaultsGroup.addBool("reward_team", () -> defaultRewardTeam, v -> defaultRewardTeam = v, false);
		defaultsGroup.addBool("consume_items", () -> defaultTeamConsumeItems, v -> defaultTeamConsumeItems = v, false);
		defaultsGroup.addBool("autoclaim_rewards", () -> defaultRewardAutoclaim, v -> defaultRewardAutoclaim = v, false);
		defaultsGroup.addEnum("quest_shape", () -> defaultShape, v -> defaultShape = v, EnumQuestShape.NAME_MAP.withDefault(EnumQuestShape.CIRCLE));

		ConfigGroup d = config.getGroup("loot_crate_no_drop");
		d.addInt("passive", () -> lootCrateNoDrop.passive, v -> lootCrateNoDrop.passive = v, 0, 0, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.loot.entitytype.passive"));
		d.addInt("monster", () -> lootCrateNoDrop.monster, v -> lootCrateNoDrop.monster = v, 0, 0, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.loot.entitytype.monster"));
		d.addInt("boss", () -> lootCrateNoDrop.boss, v -> lootCrateNoDrop.boss = v, 0, 0, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.loot.entitytype.boss"));
	}

	@Override
	public void clearCachedData()
	{
		super.clearCachedData();

		for (QuestChapter chapter : chapters)
		{
			chapter.clearCachedData();
		}

		new ClearFileCacheEvent(this).post();
	}

	public int readID(int id)
	{
		while (id == 0 || id == 1 || map.get(id) != null)
		{
			id = MathUtils.RAND.nextInt();
		}

		return id;
	}

	public static int getID(String id)
	{
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
		return 255;
	}

	public final Collection<QuestObjectBase> getAllObjects()
	{
		return map.values();
	}

	@Override
	public void clearCachedProgress(short id)
	{
		super.clearCachedProgress(id);

		for (QuestChapter chapter : chapters)
		{
			chapter.clearCachedProgress(id);

			for (Quest quest : chapter.quests)
			{
				quest.clearCachedProgress(id);

				for (QuestTask task : quest.tasks)
				{
					task.clearCachedProgress(id);
				}
			}
		}
	}

	public int getUnclaimedRewards(UUID player, QuestData data, boolean showExcluded)
	{
		int r = 0;

		for (QuestChapter chapter : chapters)
		{
			for (Quest quest : chapter.quests)
			{
				r += quest.getUnclaimedRewards(player, data, showExcluded);
			}
		}

		return r;
	}

	@Override
	public boolean isVisible(QuestData data)
	{
		for (QuestChapter chapter : chapters)
		{
			if (chapter.isVisible(data))
			{
				return true;
			}
		}

		return false;
	}

	public List<QuestChapter> getVisibleChapters(QuestData data, boolean excludeEmpty)
	{
		List<QuestChapter> list = new ArrayList<>();

		for (QuestChapter chapter : chapters)
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
}