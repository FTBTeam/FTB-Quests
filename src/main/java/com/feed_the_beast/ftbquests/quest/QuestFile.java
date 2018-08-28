package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigList;
import com.feed_the_beast.ftblib.lib.config.ConfigTimer;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.item.ItemStackSerializer;
import com.feed_the_beast.ftblib.lib.math.Ticks;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LatvianModder
 */
public abstract class QuestFile extends QuestObject
{
	public final List<QuestChapter> chapters;

	public final Map<String, QuestObject> map;
	public final List<QuestTask> allTasks;
	public final Int2ObjectOpenHashMap<QuestReward> allRewards;

	public boolean allowTakeQuestBlocks;
	public final List<ItemStack> emergencyItems;
	public Ticks emergencyItemsCooldown;

	public QuestFile()
	{
		id = "*";
		chapters = new ArrayList<>();

		map = new HashMap<>();
		allTasks = new ArrayList<>();
		allRewards = new Int2ObjectOpenHashMap<>();

		allowTakeQuestBlocks = true;
		emergencyItems = new ArrayList<>();
		emergencyItems.add(new ItemStack(Items.APPLE));
		emergencyItemsCooldown = Ticks.MINUTE.x(5);
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

		allTasks.clear();

		for (QuestChapter chapter : chapters)
		{
			for (Quest quest : chapter.quests)
			{
				for (QuestTask task : quest.tasks)
				{
					task.index = allTasks.size();
					allTasks.add(task);
				}
			}
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
	public QuestTask getTaskByIndex(int index)
	{
		return index < 0 || index >= allTasks.size() ? null : allTasks.get(index);
	}

	@Nullable
	public QuestObject createAndAdd(QuestObjectType type, String parent, NBTTagCompound nbt)
	{
		switch (type)
		{
			case CHAPTER:
			{
				QuestChapter chapter = new QuestChapter(this, nbt);
				chapter.index = chapter.file.chapters.size();
				chapter.file.chapters.add(chapter);
				return chapter;
			}
			case QUEST:
			{
				QuestChapter chapter = getChapter(parent);

				if (chapter != null)
				{
					Quest quest = new Quest(chapter, nbt);
					chapter.quests.add(quest);
					return quest;
				}

				return null;
			}
			case TASK:
			{
				Quest quest = getQuest(parent);

				if (quest != null)
				{
					QuestTask task = QuestTaskType.createTask(quest, nbt);

					if (task != null)
					{
						quest.tasks.add(task);
						return task;
					}
				}

				return null;
			}
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

		NBTTagList chaptersList = new NBTTagList();

		for (QuestChapter chapter : chapters)
		{
			NBTTagCompound chapterNBT = new NBTTagCompound();
			chapter.writeData(chapterNBT);
			chaptersList.appendTag(chapterNBT);
		}

		nbt.setTag("chapters", chaptersList);
		nbt.setBoolean("allow_take_quest_blocks", allowTakeQuestBlocks);

		NBTTagList emergencyItemsList = new NBTTagList();

		for (ItemStack stack : emergencyItems)
		{
			emergencyItemsList.appendTag(ItemStackSerializer.write(stack));
		}

		nbt.setTag("emergency_items", emergencyItemsList);
		nbt.setString("emergency_items_cooldown", emergencyItemsCooldown.toString());
	}

	protected final void readData(NBTTagCompound nbt)
	{
		title = nbt.getString("title");
		icon = ItemStackSerializer.read(nbt.getCompoundTag("icon"));

		chapters.clear();

		NBTTagList chapterList = nbt.getTagList("chapters", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < chapterList.tagCount(); i++)
		{
			QuestChapter chapter = new QuestChapter(this, chapterList.getCompoundTagAt(i));
			chapter.index = chapters.size();
			chapters.add(chapter);
		}

		refreshIDMap();

		allowTakeQuestBlocks = !nbt.hasKey("allow_take_quest_blocks") || nbt.getBoolean("allow_take_quest_blocks");
		emergencyItems.clear();

		NBTTagList emergencyItemsList = nbt.getTagList("emergency_items", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < emergencyItemsList.tagCount(); i++)
		{
			ItemStack stack = ItemStackSerializer.read(emergencyItemsList.getCompoundTagAt(i));

			if (!stack.isEmpty())
			{
				emergencyItems.add(stack);
			}
		}

		Ticks t = Ticks.get(nbt.getString("emergency_items_cooldown"));
		emergencyItemsCooldown = t.hasTicks() ? t : Ticks.MINUTE.x(5);
	}

	@Nullable
	public abstract ITeamData getData(String team);

	public abstract Collection<? extends ITeamData> getAllData();

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
		config.add("allow_take_quest_blocks", new ConfigBoolean(false)
		{
			@Override
			public boolean getBoolean()
			{
				return allowTakeQuestBlocks;
			}

			@Override
			public void setBoolean(boolean v)
			{
				allowTakeQuestBlocks = v;
			}
		}, new ConfigBoolean(true));

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