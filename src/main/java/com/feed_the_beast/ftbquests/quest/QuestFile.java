package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.FTBLibConfig;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigTimer;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.math.MathUtils;
import com.feed_the_beast.ftblib.lib.math.Ticks;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.item.ItemMissing;
import com.feed_the_beast.ftbquests.item.LootRarity;
import com.feed_the_beast.ftbquests.quest.reward.FTBQuestsRewards;
import com.feed_the_beast.ftbquests.quest.reward.ItemReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import com.feed_the_beast.ftbquests.quest.reward.RewardTable;
import com.feed_the_beast.ftbquests.quest.task.FTBQuestsTasks;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public abstract class QuestFile extends QuestObject
{
	public static final int VERSION = 3;

	public final List<QuestChapter> chapters;
	public final List<QuestVariable> variables;
	public final List<RewardTable> rewardTables;

	private final Int2ObjectOpenHashMap<QuestObjectBase> map;
	private final Object2IntOpenHashMap<String> oldMap;

	public final List<ItemStack> emergencyItems;
	public Ticks emergencyItemsCooldown;
	public final ResourceLocation[] lootTables;
	public int lootSize;
	public boolean defaultRewardTeam;
	public boolean defaultCheckOnly;
	public int fileVersion;
	public final RewardTable dummyTable;

	public QuestFile()
	{
		uid = 1;
		chapters = new ArrayList<>();
		variables = new ArrayList<>();
		rewardTables = new ArrayList<>();

		map = new Int2ObjectOpenHashMap<>();
		oldMap = new Object2IntOpenHashMap<>();

		emergencyItems = new ArrayList<>();
		emergencyItems.add(new ItemStack(Items.APPLE));
		emergencyItemsCooldown = Ticks.MINUTE.x(5);

		lootTables = new ResourceLocation[LootRarity.VALUES.length];

		for (LootRarity rarity : LootRarity.VALUES)
		{
			lootTables[rarity.ordinal()] = rarity.getLootTable();
		}

		lootSize = 27;
		defaultRewardTeam = false;
		defaultCheckOnly = false;
		fileVersion = 0;
		dummyTable = new RewardTable(this);
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

	@Override
	public long getProgress(ITeamData data)
	{
		long progress = 0L;

		for (QuestChapter chapter : chapters)
		{
			progress += chapter.getProgress(data);
		}

		return progress;
	}

	@Override
	public long getMaxProgress()
	{
		long maxProgress = 0L;

		for (QuestChapter chapter : chapters)
		{
			maxProgress += chapter.getMaxProgress();
		}

		return maxProgress;
	}

	@Override
	public int getRelativeProgress(ITeamData data)
	{
		int progress = 0;

		for (QuestChapter chapter : chapters)
		{
			progress += chapter.getRelativeProgress(data);
		}

		return fixRelativeProgress(progress, chapters.size());
	}

	@Override
	public boolean isComplete(ITeamData data)
	{
		for (QuestChapter chapter : chapters)
		{
			if (!chapter.isComplete(data))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public void onCompleted(ITeamData data)
	{
		super.onCompleted(data);
		new ObjectCompletedEvent.FileEvent(data, this).post();
	}

	@Override
	public void resetProgress(ITeamData data, boolean dependencies)
	{
		for (QuestChapter chapter : chapters)
		{
			chapter.resetProgress(data, dependencies);
		}
	}

	@Override
	public void completeInstantly(ITeamData data, boolean dependencies)
	{
		for (QuestChapter chapter : chapters)
		{
			chapter.completeInstantly(data, dependencies);
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

		for (QuestVariable variable : variables)
		{
			variable.deleteChildren();
			variable.invalid = true;
		}

		variables.clear();

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
	public QuestVariable getVariable(int id)
	{
		QuestObjectBase object = getBase(id);
		return object instanceof QuestVariable ? (QuestVariable) object : null;
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

	public void refreshIDMap()
	{
		clearCachedData();
		map.clear();

		for (RewardTable table : rewardTables)
		{
			map.put(table.uid, table);
		}

		for (QuestChapter chapter : chapters)
		{
			map.put(chapter.uid, chapter);

			for (Quest quest : chapter.quests)
			{
				map.put(quest.uid, quest);

				for (QuestTask task : quest.tasks)
				{
					map.put(task.uid, task);
				}

				for (QuestReward reward : quest.rewards)
				{
					map.put(reward.uid, reward);
				}
			}
		}

		for (QuestVariable variable : variables)
		{
			map.put(variable.uid, variable);
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
			case VARIABLE:
				return new QuestVariable(this);
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
		nbt.setBoolean("default_check_only", defaultCheckOnly);

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

		for (LootRarity rarity : LootRarity.VALUES)
		{
			if (!lootTables[rarity.ordinal()].equals(rarity.getLootTable()))
			{
				nbt.setString(rarity.getName() + "_loot_table", lootTables[rarity.ordinal()].toString());
			}
		}

		nbt.setShort("loot_size", (short) lootSize);
	}

	@Override
	public final void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		defaultRewardTeam = nbt.getBoolean("default_reward_team");
		defaultCheckOnly = nbt.getBoolean("default_check_only");
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

		for (LootRarity rarity : LootRarity.VALUES)
		{
			String s = nbt.getString(rarity.getName() + "_loot_table");
			lootTables[rarity.ordinal()] = s.isEmpty() ? rarity.getLootTable() : new ResourceLocation(s);
		}

		lootSize = nbt.getShort("loot_size") & 0xFFFF;

		if (lootSize == 0)
		{
			lootSize = 27;
		}
	}

	public final void writeDataFull(NBTTagCompound nbt)
	{
		writeData(nbt);

		if (!oldMap.isEmpty())
		{
			NBTTagCompound oids = new NBTTagCompound();

			for (Object2IntOpenHashMap.Entry<String> entry : oldMap.object2IntEntrySet())
			{
				oids.setInteger(entry.getKey(), entry.getIntValue());
			}

			nbt.setTag("old_ids", oids);
		}

		NBTTagList rt = new NBTTagList();

		for (RewardTable table : rewardTables)
		{
			NBTTagCompound nbt1 = new NBTTagCompound();
			table.writeData(nbt1);
			nbt1.setInteger("uid", table.uid);
			rt.appendTag(nbt1);
		}

		nbt.setTag("reward_tables", rt);

		NBTTagList c = new NBTTagList();

		for (QuestChapter chapter : chapters)
		{
			NBTTagCompound nbt1 = new NBTTagCompound();
			chapter.writeData(nbt1);
			nbt1.setInteger("uid", chapter.uid);
			c.appendTag(nbt1);

			if (!chapter.quests.isEmpty())
			{
				NBTTagList q = new NBTTagList();

				for (Quest quest : chapter.quests)
				{
					NBTTagCompound nbt2 = new NBTTagCompound();
					quest.writeData(nbt2);
					nbt2.setInteger("uid", quest.uid);
					q.appendTag(nbt2);

					if (!quest.tasks.isEmpty())
					{
						NBTTagList t = new NBTTagList();

						for (QuestTask task : quest.tasks)
						{
							QuestTaskType type = task.getType();
							NBTTagCompound nbt3 = new NBTTagCompound();
							task.writeData(nbt3);
							nbt3.setInteger("uid", task.uid);

							if (type != FTBQuestsTasks.ITEM)
							{
								nbt3.setString("type", type.getTypeForNBT());
							}

							t.appendTag(nbt3);
						}

						if (t.tagCount() == 1)
						{
							nbt2.setTag("task", t.get(0));
						}
						else if (!t.isEmpty())
						{
							nbt2.setTag("tasks", t);
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
							nbt3.setInteger("uid", reward.uid);

							if (type != FTBQuestsRewards.ITEM)
							{
								nbt3.setString("type", type.getTypeForNBT());
							}

							r.appendTag(nbt3);
						}

						if (r.tagCount() == 1)
						{
							nbt2.setTag("reward", r.get(0));
						}
						else if (!r.isEmpty())
						{
							nbt2.setTag("rewards", r);
						}
					}
				}

				nbt1.setTag("quests", q);
			}
		}

		nbt.setTag("chapters", c);

		NBTTagList v = new NBTTagList();

		for (QuestVariable variable : variables)
		{
			NBTTagCompound nbt1 = new NBTTagCompound();
			variable.writeData(nbt1);
			nbt1.setInteger("uid", variable.uid);
			v.appendTag(nbt1);
		}

		nbt.setTag("variables", v);
	}

	private void readIDs(QuestObjectBase object, NBTTagCompound nbt)
	{
		object.uid = readID(nbt.getInteger("uid"));

		if (object instanceof QuestObject)
		{
			QuestObject o = (QuestObject) object;
			String id = nbt.getString("id").trim();

			if (!id.isEmpty() && !id.equalsIgnoreCase(object.getCodeString()))
			{
				if (o instanceof QuestVariable)
				{
					oldMap.put("#" + id, o.uid);
				}
				else if (o instanceof QuestChapter)
				{
					oldMap.put(id, o.uid);
				}
				else if (o instanceof Quest)
				{
					String c = getOldID(((Quest) o).chapter);

					if (!c.isEmpty())
					{
						oldMap.put(c + ":" + id, o.uid);
					}
				}
				else if (o instanceof QuestTask)
				{
					String q = getOldID(((QuestTask) o).quest);

					if (!q.isEmpty())
					{
						oldMap.put(q + ":" + id, o.uid);
					}
				}
			}
		}
	}

	public final void readDataFull(NBTTagCompound nbt)
	{
		fileVersion = nbt.getInteger("version");
		readData(nbt);

		chapters.clear();
		variables.clear();
		rewardTables.clear();

		oldMap.clear();

		NBTTagCompound oids = nbt.getCompoundTag("old_ids");

		for (String s : oids.getKeySet())
		{
			oldMap.put(s, oids.getInteger(s));
		}

		NBTTagList rtl = nbt.getTagList("reward_tables", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < rtl.tagCount(); i++)
		{
			RewardTable table = new RewardTable(this);
			NBTTagCompound nbt1 = rtl.getCompoundTagAt(i);
			readIDs(table, nbt1);
			table.readData(nbt1);
			rewardTables.add(table);
		}

		NBTTagList c = nbt.getTagList("chapters", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < c.tagCount(); i++)
		{
			QuestChapter chapter = new QuestChapter(this);
			NBTTagCompound ct = c.getCompoundTagAt(i);
			readIDs(chapter, ct);
			chapters.add(chapter);

			NBTTagList q = ct.getTagList("quests", Constants.NBT.TAG_COMPOUND);

			for (int j = 0; j < q.tagCount(); j++)
			{
				Quest quest = new Quest(chapter);
				NBTTagCompound qt = q.getCompoundTagAt(j);
				readIDs(quest, qt);
				chapter.quests.add(quest);

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
						readIDs(task, tt);
						task.readData(tt);
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

					if (!rt.hasKey("type") && !rt.hasKey("item"))
					{
						ItemReward reward = new ItemReward(quest);
						reward.uid = readID(rt.getInteger("uid"));
						reward.team = rt.getBoolean("team_reward");
						rt.removeTag("uid");
						rt.removeTag("team_reward");
						reward.stack = ItemMissing.read(rt);
						quest.rewards.add(reward);
						continue;
					}

					QuestReward reward = QuestRewardType.createReward(quest, rt.getString("type"));

					if (reward != null)
					{
						readIDs(reward, rt);
						reward.readData(rt);
						quest.rewards.add(reward);
					}
				}
			}
		}

		NBTTagList v = nbt.getTagList("variables", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < v.tagCount(); i++)
		{
			QuestVariable variable = new QuestVariable(this);
			NBTTagCompound nbt1 = v.getCompoundTagAt(i);
			readIDs(variable, nbt1);
			variable.readData(nbt1);
			variables.add(variable);
		}

		refreshIDMap();

		for (int i = 0; i < chapters.size(); i++)
		{
			QuestChapter chapter = chapters.get(i);
			NBTTagCompound ct = c.getCompoundTagAt(i);
			chapter.readData(ct);

			NBTTagList q = ct.getTagList("quests", Constants.NBT.TAG_COMPOUND);

			for (int j = 0; j < q.tagCount(); j++)
			{
				Quest quest = chapter.quests.get(j);
				NBTTagCompound qt = q.getCompoundTagAt(j);
				quest.readData(qt);
			}
		}

		for (QuestChapter chapter : chapters)
		{
			for (Quest quest : chapter.quests)
			{
				quest.verifyDependencies();
			}
		}
	}

	@Override
	public final void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeCollection(emergencyItems, DataOut.ITEM_STACK);
		data.writeVarLong(emergencyItemsCooldown.ticks());

		for (LootRarity rarity : LootRarity.VALUES)
		{
			data.writeResourceLocation(lootTables[rarity.ordinal()]);
		}

		data.writeVarInt(lootSize);
		data.writeBoolean(defaultRewardTeam);
		data.writeBoolean(defaultCheckOnly);
	}

	@Override
	public final void readNetData(DataIn data)
	{
		super.readNetData(data);
		data.readCollection(emergencyItems, DataIn.ITEM_STACK);
		emergencyItemsCooldown = Ticks.get(data.readVarLong());

		for (LootRarity rarity : LootRarity.VALUES)
		{
			lootTables[rarity.ordinal()] = data.readResourceLocation();
		}

		lootSize = data.readVarInt();
		defaultRewardTeam = data.readBoolean();
		defaultCheckOnly = data.readBoolean();
	}

	public final void writeNetDataFull(DataOut data)
	{
		int pos = data.getPosition();
		writeNetData(data);
		data.writeVarInt(oldMap.size());

		for (Object2IntOpenHashMap.Entry<String> entry : oldMap.object2IntEntrySet())
		{
			data.writeString(entry.getKey());
			data.writeInt(entry.getIntValue());
		}

		data.writeVarInt(rewardTables.size());

		for (RewardTable table : rewardTables)
		{
			data.writeInt(table.uid);
		}

		data.writeVarInt(chapters.size());

		ForgeRegistry<QuestTaskType> taskTypes = QuestTaskType.getRegistry();
		ForgeRegistry<QuestRewardType> rewardTypes = QuestRewardType.getRegistry();

		for (QuestChapter chapter : chapters)
		{
			data.writeInt(chapter.uid);
			data.writeVarInt(chapter.quests.size());

			for (Quest quest : chapter.quests)
			{
				data.writeInt(quest.uid);
				data.writeVarInt(quest.tasks.size());

				for (QuestTask task : quest.tasks)
				{
					data.writeVarInt(taskTypes.getID(task.getType()));
					data.writeInt(task.uid);
				}

				data.writeVarInt(quest.rewards.size());

				for (QuestReward reward : quest.rewards)
				{
					data.writeVarInt(rewardTypes.getID(reward.getType()));
					data.writeInt(reward.uid);
				}
			}
		}

		data.writeVarInt(variables.size());

		for (QuestVariable variable : variables)
		{
			data.writeInt(variable.uid);
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

		for (QuestVariable variable : variables)
		{
			variable.writeNetData(data);
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

		oldMap.clear();
		int oids = data.readVarInt();

		for (int i = 0; i < oids; i++)
		{
			String k = data.readString();
			int v = data.readInt();
			oldMap.put(k, v);
		}

		chapters.clear();
		variables.clear();
		rewardTables.clear();

		int rtl = data.readVarInt();

		for (int i = 0; i < rtl; i++)
		{
			RewardTable table = new RewardTable(this);
			table.uid = data.readInt();
			rewardTables.add(table);
		}

		ForgeRegistry<QuestTaskType> taskTypes = QuestTaskType.getRegistry();
		ForgeRegistry<QuestRewardType> rewardTypes = QuestRewardType.getRegistry();

		int c = data.readVarInt();

		for (int i = 0; i < c; i++)
		{
			QuestChapter chapter = new QuestChapter(this);
			chapter.uid = data.readInt();
			chapters.add(chapter);

			int q = data.readVarInt();

			for (int j = 0; j < q; j++)
			{
				Quest quest = new Quest(chapter);
				quest.uid = data.readInt();
				chapter.quests.add(quest);

				int t = data.readVarInt();

				for (int k = 0; k < t; k++)
				{
					QuestTaskType type = taskTypes.getValue(data.readVarInt());
					QuestTask task = type.provider.create(quest);
					task.uid = data.readInt();
					quest.tasks.add(task);
				}

				int r = data.readVarInt();

				for (int k = 0; k < r; k++)
				{
					QuestRewardType type = rewardTypes.getValue(data.readVarInt());
					QuestReward reward = type.provider.create(quest);
					reward.uid = data.readInt();
					quest.rewards.add(reward);
				}
			}
		}

		int v = data.readVarInt();

		for (int i = 0; i < v; i++)
		{
			QuestVariable variable = new QuestVariable(this);
			variable.uid = data.readInt();
			variables.add(variable);
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

		for (QuestVariable variable : variables)
		{
			variable.readNetData(data);
		}

		if (FTBLibConfig.debugging.print_more_info)
		{
			FTBQuests.LOGGER.info("Read " + (data.getPosition() - pos) + " bytes");
		}
	}

	@Nullable
	public abstract ITeamData getData(short team);

	@Nullable
	public abstract ITeamData getData(String team);

	public abstract Collection<? extends ITeamData> getAllData();

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
	public ITextComponent getAltDisplayName()
	{
		return new TextComponentTranslation("ftbquests.file");
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
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

		ConfigGroup defaultsGroup = config.getGroup("defaults");
		defaultsGroup.addBool("reward_team", () -> defaultRewardTeam, v -> defaultRewardTeam = v, false);
		defaultsGroup.addBool("check_only", () -> defaultCheckOnly, v -> defaultCheckOnly = v, false);

		ConfigGroup lootGroup = config.getGroup("loot");
		lootGroup.addInt("size", () -> lootSize, v -> lootSize = v, 27, 1, 1024);

		Pattern pattern = Pattern.compile("[a-z0-9_]+:.+");

		for (LootRarity r : LootRarity.VALUES)
		{
			lootGroup.addString(r.getName(), () -> lootTables[r.ordinal()].toString(), v -> lootTables[r.ordinal()] = v.equals(r.getLootTable().toString()) ? r.getLootTable() : new ResourceLocation(v), r.getLootTable().toString(), pattern).setDisplayName(new TextComponentTranslation(r.getTranslationKey()));
		}
	}

	@Override
	public void clearCachedData()
	{
		super.clearCachedData();

		for (QuestChapter chapter : chapters)
		{
			chapter.clearCachedData();
		}
	}

	public int readID(int id)
	{
		while (id == 0 || id == 1 || map.get(id) != null)
		{
			id = MathUtils.RAND.nextInt();
		}

		return id;
	}

	public int getID(String id)
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
			return oldMap.getInt(id);
		}
	}

	public int getID(@Nullable NBTBase nbt)
	{
		if (nbt == null || nbt.isEmpty())
		{
			return 0;
		}
		else if (nbt instanceof NBTTagString)
		{
			return getID(((NBTTagString) nbt).getString());
		}
		else if (nbt instanceof NBTPrimitive)
		{
			return ((NBTPrimitive) nbt).getInt();
		}

		return 0;
	}

	public String getOldID(QuestObject object)
	{
		for (Object2IntOpenHashMap.Entry<String> entry : oldMap.object2IntEntrySet())
		{
			if (entry.getIntValue() == object.uid)
			{
				return entry.getKey();
			}
		}

		return "";
	}
}