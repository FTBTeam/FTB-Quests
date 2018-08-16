package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigBoolean;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigList;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.config.ConfigTimer;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.item.ItemStackSerializer;
import com.feed_the_beast.ftblib.lib.math.Ticks;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.rewards.QuestRewards;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTasks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
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
public abstract class QuestFile extends ProgressingQuestObject
{
	public final ConfigString title;
	public final ConfigItemStack icon;
	public final List<QuestChapter> chapters;

	private final Map<String, QuestObject> map;
	public final List<QuestTask> allTasks;
	public final List<QuestTask> allItemAcceptingTasks;

	public final ConfigBoolean allowTakeQuestBlocks;
	public final ConfigList<ConfigItemStack> emergencyItems;
	public final ConfigTimer emergencyItemsCooldown;

	public QuestFile()
	{
		id = "*";
		title = new ConfigString("");
		icon = new ConfigItemStack(ItemStack.EMPTY);
		chapters = new ArrayList<>();

		map = new HashMap<>();
		allTasks = new ArrayList<>();
		allItemAcceptingTasks = new ArrayList<>();

		allowTakeQuestBlocks = new ConfigBoolean(true);
		emergencyItems = new ConfigList<>(new ConfigItemStack(new ItemStack(Items.APPLE)));
		emergencyItemsCooldown = new ConfigTimer(Ticks.MINUTE.x(5));
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
	public long getProgress(IProgressData data)
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
	public double getRelativeProgress(IProgressData data)
	{
		double progress = 0D;

		for (QuestChapter chapter : chapters)
		{
			progress += chapter.getRelativeProgress(data);
		}

		return progress / (double) chapters.size();
	}

	@Override
	public final boolean isComplete(IProgressData data)
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
	public void resetProgress(IProgressData data)
	{
		for (QuestChapter chapter : chapters)
		{
			chapter.resetProgress(data);
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
			return object;
		}

		return null;
	}

	@Nullable
	public ProgressingQuestObject getProgressing(String id)
	{
		QuestObject object = get(id);
		return object instanceof ProgressingQuestObject ? (ProgressingQuestObject) object : null;
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
	public QuestReward getReward(String id)
	{
		QuestObject object = get(id);
		return object instanceof QuestReward ? (QuestReward) object : null;
	}

	public void refreshTaskList()
	{
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

		allItemAcceptingTasks.clear();

		for (QuestTask task : allTasks)
		{
			if (task.canInsertItem())
			{
				allItemAcceptingTasks.add(task);
			}
		}
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

				for (QuestReward reward : quest.rewards)
				{
					map.put(reward.getID(), reward);
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
					if (quest.tasks.size() >= 256)
					{
						return null;
					}

					QuestTask task = QuestTasks.createTask(quest, nbt);
					quest.tasks.add(task);
					return task;
				}

				return null;
			}
			case REWARD:
			{
				Quest quest = getQuest(parent);

				if (quest != null)
				{
					if (quest.rewards.size() >= 256)
					{
						return null;
					}

					QuestReward reward = QuestRewards.createReward(quest, nbt);
					quest.rewards.add(reward);
					return reward;
				}

				return null;
			}
			default:
				return null;
		}
	}

	public static ItemStack getIcon(NBTTagCompound nbt)
	{
		ItemStack stack;

		if (nbt.hasKey("icon", Constants.NBT.TAG_STRING))
		{
			stack = ItemStackSerializer.parseItem(nbt.getString("icon"));
		}
		else
		{
			stack = new ItemStack(nbt.getCompoundTag("icon"));
		}

		return stack.isEmpty() ? ItemStack.EMPTY : stack;
	}

	@Override
	public final void writeData(NBTTagCompound nbt)
	{
		nbt.setString("title", title.getString());
		nbt.setTag("icon", icon.getStack().serializeNBT());

		NBTTagList chaptersList = new NBTTagList();

		for (QuestChapter chapter : chapters)
		{
			NBTTagCompound chapterNBT = new NBTTagCompound();
			chapter.writeData(chapterNBT);
			chaptersList.appendTag(chapterNBT);
		}

		nbt.setTag("chapters", chaptersList);
		nbt.setBoolean("allow_take_quest_blocks", allowTakeQuestBlocks.getBoolean());

		NBTTagList emergencyItemsList = new NBTTagList();

		for (ConfigItemStack value : emergencyItems)
		{
			emergencyItemsList.appendTag(value.getStack().serializeNBT());
		}

		nbt.setTag("emergency_items", emergencyItemsList);
		nbt.setString("emergency_items_cooldown", emergencyItemsCooldown.getTimer().toString());
	}

	protected final void readData(NBTTagCompound nbt)
	{
		chapters.clear();

		title.setString(nbt.getString("title"));
		icon.setStack(new ItemStack(nbt.getCompoundTag("icon")));

		NBTTagList chapterList = nbt.getTagList("chapters", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < chapterList.tagCount(); i++)
		{
			QuestChapter chapter = new QuestChapter(this, chapterList.getCompoundTagAt(i));
			chapter.index = chapters.size();
			chapters.add(chapter);
		}

		refreshIDMap();
		refreshTaskList();

		allowTakeQuestBlocks.setBoolean(!nbt.hasKey("allow_take_quest_blocks") || nbt.getBoolean("allow_take_quest_blocks"));
		emergencyItems.list.clear();

		NBTTagList emergencyItemsList = nbt.getTagList("emergency_items", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < emergencyItemsList.tagCount(); i++)
		{
			ItemStack stack = new ItemStack(emergencyItemsList.getCompoundTagAt(i));

			if (!stack.isEmpty())
			{
				emergencyItems.add(new ConfigItemStack(stack));
			}
		}

		Ticks t = Ticks.get(nbt.getString("emergency_items_cooldown"));
		emergencyItemsCooldown.setTimer(t.hasTicks() ? t : Ticks.MINUTE.x(5));
	}

	@Nullable
	public abstract IProgressData getData(String team);

	public abstract Collection<IProgressData> getAllData();

	@Override
	public Icon getIcon()
	{
		if (!icon.isEmpty())
		{
			return ItemIcon.getItemIcon(icon.getStack());
		}

		return GuiIcons.BOOK_RED;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		if (!title.isEmpty())
		{
			return new TextComponentString(title.getString());
		}

		return new TextComponentTranslation("ftbquests.file");
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		config.add("title", title, new ConfigString(""));
		config.add("icon", icon, new ConfigItemStack(ItemStack.EMPTY));
		config.add("allow_take_quest_blocks", allowTakeQuestBlocks, new ConfigBoolean(true));
		config.add("emergency_items", emergencyItems, new ConfigList<>(new ConfigItemStack(new ItemStack(Items.APPLE))));
		config.add("emergency_items_cooldown", emergencyItemsCooldown, new ConfigTimer(Ticks.MINUTE.x(5)));
	}
}