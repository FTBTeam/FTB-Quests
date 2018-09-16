package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigList;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.config.ConfigTimer;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.item.ItemStackSerializer;
import com.feed_the_beast.ftblib.lib.math.Ticks;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.item.LootRarity;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public abstract class QuestFile extends QuestObject
{
	public final List<QuestChapter> chapters;
	public final List<QuestVariable> variables;

	public final Map<String, QuestObject> map;
	public QuestTask[] allTasks;
	public final Int2ObjectOpenHashMap<QuestReward> allRewards;

	public final List<ItemStack> emergencyItems;
	public Ticks emergencyItemsCooldown;
	public String soundTask, soundQuest, soundChapter, soundFile;
	public final ResourceLocation[] lootTables;
	public int lootSize;
	public Color4I colCompleted, colStarted, colNotStarted, colCantStart, colHasRewards;

	public QuestFile()
	{
		id = "*";
		chapters = new ArrayList<>();
		variables = new ArrayList<>();

		map = new HashMap<>();
		allTasks = new QuestTask[0];
		allRewards = new Int2ObjectOpenHashMap<>();

		emergencyItems = new ArrayList<>();
		emergencyItems.add(new ItemStack(Items.APPLE));
		emergencyItemsCooldown = Ticks.MINUTE.x(5);
		soundTask = "";
		soundQuest = "";
		soundChapter = "";
		soundFile = "minecraft:ui.toast.challenge_complete";

		lootTables = new ResourceLocation[LootRarity.VALUES.length];

		for (LootRarity rarity : LootRarity.VALUES)
		{
			lootTables[rarity.ordinal()] = rarity.getLootTable();
		}

		lootSize = 9;

		colCompleted = Color4I.rgb(0x56FF56);
		colStarted = Color4I.rgb(0x00FFFF);
		colNotStarted = Color4I.rgb(0xFFFFFF);
		colCantStart = Color4I.rgb(0x999999);
		colHasRewards = Color4I.rgb(0x88C9DC);
	}

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

	@Override
	public String getID()
	{
		return id;
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
	public void resetProgress(ITeamData data)
	{
		for (QuestChapter chapter : chapters)
		{
			chapter.resetProgress(data);
		}
	}

	@Override
	public void completeInstantly(ITeamData data)
	{
		for (QuestChapter chapter : chapters)
		{
			chapter.completeInstantly(data);
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
	}

	@Nullable
	public QuestObject get(String id)
	{
		if (id.isEmpty())
		{
			return null;
		}
		else if (id.charAt(0) == '*')
		{
			return this;
		}

		QuestObject object = map.get(id);
		return object == null || object.invalid ? null : object;
	}

	@Nullable
	public QuestObject remove(String id)
	{
		QuestObject object = map.remove(id);

		if (object != null)
		{
			object.invalid = true;
			refreshIDMap();
			return object;
		}

		return null;
	}

	@Nullable
	public QuestChapter getChapter(String id)
	{
		QuestObject object = get(id);
		return object instanceof QuestChapter ? (QuestChapter) object : null;
	}

	@Nullable
	public Quest getQuest(String id)
	{
		QuestObject object = get(id);
		return object instanceof Quest ? (Quest) object : null;
	}

	@Nullable
	public QuestTask getTask(String id)
	{
		QuestObject object = get(id);
		return object instanceof QuestTask ? (QuestTask) object : null;
	}

	@Nullable
	public QuestVariable getVariable(String id)
	{
		QuestObject object = get(id);
		return object instanceof QuestVariable ? (QuestVariable) object : null;
	}

	public void refreshIDMap()
	{
		map.clear();
		map.put("*", this);

		for (QuestChapter chapter : chapters)
		{
			map.put(chapter.getID(), chapter);

			for (Quest quest : chapter.quests)
			{
				map.put(quest.getID(), quest);

				for (QuestTask task : quest.tasks)
				{
					map.put(task.getID(), task);
				}
			}
		}

		for (QuestVariable variable : variables)
		{
			map.put(variable.getID(), variable);
		}

		List<QuestTask> tasks = new ArrayList<>();

		for (int i = 0; i < chapters.size(); i++)
		{
			QuestChapter chapter = chapters.get(i);
			chapter.chapterIndex = i;

			for (Quest quest : chapter.quests)
			{
				for (QuestTask task : quest.tasks)
				{
					task.index = (short) tasks.size();
					tasks.add(task);
				}
			}
		}

		allTasks = tasks.toArray(new QuestTask[0]);

		for (int i = 0; i < variables.size(); i++)
		{
			variables.get(i).index = (short) i;
		}

		allRewards.clear();

		for (QuestChapter chapter : chapters)
		{
			for (Quest quest : chapter.quests)
			{
				for (QuestReward reward : quest.rewards)
				{
					allRewards.put(reward.uid, reward);
				}
			}
		}
	}

	@Nullable
	public QuestObject create(QuestObjectType type, String parent, NBTTagCompound nbt)
	{
		switch (type)
		{
			case CHAPTER:
				return new QuestChapter(this, nbt);
			case QUEST:
			{
				QuestChapter chapter = getChapter(parent);

				if (chapter != null)
				{
					return new Quest(chapter, nbt);
				}

				return null;
			}
			case TASK:
			{
				Quest quest = getQuest(parent);

				if (quest != null)
				{
					return QuestTaskType.createTask(quest, nbt);
				}

				return null;
			}
			case VARIABLE:
				return new QuestVariable(this, nbt);
			default:
				return null;
		}
	}

	@Override
	public final void writeData(NBTTagCompound nbt)
	{
		if (!title.isEmpty())
		{
			nbt.setString("title", title);
		}

		if (!icon.isEmpty())
		{
			nbt.setTag("icon", ItemStackSerializer.write(icon));
		}

		if (!completionCommand.isEmpty())
		{
			nbt.setString("completion_command", completionCommand);
		}

		NBTTagList list = new NBTTagList();

		for (QuestChapter chapter : chapters)
		{
			NBTTagCompound chapterNBT = new NBTTagCompound();
			chapter.writeData(chapterNBT);
			chapterNBT.setString("id", chapter.id);
			list.appendTag(chapterNBT);
		}

		nbt.setTag("chapters", list);

		list = new NBTTagList();

		for (QuestVariable variable : variables)
		{
			NBTTagCompound variableNBT = new NBTTagCompound();
			variable.writeData(variableNBT);
			variableNBT.setString("id", variable.id);
			list.appendTag(variableNBT);
		}

		nbt.setTag("variables", list);

		list = new NBTTagList();

		for (ItemStack stack : emergencyItems)
		{
			list.appendTag(ItemStackSerializer.write(stack));
		}

		nbt.setTag("emergency_items", list);
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

	protected final void readData(NBTTagCompound nbt)
	{
		title = nbt.getString("title");
		icon = ItemStackSerializer.read(nbt.getCompoundTag("icon"));
		completionCommand = nbt.getString("completion_command");

		chapters.clear();

		NBTTagList list = nbt.getTagList("chapters", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); i++)
		{
			QuestChapter chapter = new QuestChapter(this, list.getCompoundTagAt(i));
			chapter.chapterIndex = chapters.size();
			chapters.add(chapter);
		}

		variables.clear();

		list = nbt.getTagList("variables", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); i++)
		{
			QuestVariable variable = new QuestVariable(this, list.getCompoundTagAt(i));
			variable.index = (short) variables.size();
			variables.add(variable);
		}

		refreshIDMap();

		emergencyItems.clear();

		list = nbt.getTagList("emergency_items", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); i++)
		{
			ItemStack stack = ItemStackSerializer.read(list.getCompoundTagAt(i));

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
			lootSize = 9;
		}
	}

	@Nullable
	public abstract ITeamData getData(String team);

	public abstract Collection<? extends ITeamData> getAllData();

	public abstract void deleteObject(String id);

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
		config.add("emergency_items", new ConfigList<ConfigItemStack>(new ConfigItemStack(ItemStack.EMPTY))
		{
			@Override
			public void writeToList()
			{
				list.clear();

				for (ItemStack stack : emergencyItems)
				{
					list.add(new ConfigItemStack(stack));
				}
			}

			@Override
			public void readFromList()
			{
				emergencyItems.clear();

				for (ConfigItemStack value : list)
				{
					emergencyItems.add(value.getStack());
				}
			}
		}, new ConfigList<>(new ConfigItemStack(new ItemStack(Items.APPLE))));

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

		ConfigGroup lootGroup = config.getGroup("loot_tables");
		Pattern pattern = Pattern.compile("[a-z0-9_]+:.+");

		for (LootRarity rarity : LootRarity.VALUES)
		{
			lootGroup.add(rarity.getName(), new ConfigString("", pattern)
			{
				@Override
				public String getString()
				{
					return lootTables[rarity.ordinal()].toString();
				}

				@Override
				public void setString(String v)
				{
					lootTables[rarity.ordinal()] = v.equals(rarity.getLootTable().toString()) ? rarity.getLootTable() : new ResourceLocation(v);
				}
			}, new ConfigString(rarity.getLootTable().toString())).setDisplayName(new TextComponentTranslation(rarity.getTranslationKey()));
		}

		config.add("loot_size", new ConfigInt(0, 1, 1024)
		{
			@Override
			public int getInt()
			{
				return lootSize;
			}

			@Override
			public void setInt(int v)
			{
				lootSize = v;
			}
		}, new ConfigInt(9));
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
}