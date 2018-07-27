package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigList;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.item.ItemStackSerializer;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.rewards.QuestRewards;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTasks;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
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
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestList extends ProgressingQuestObject
{
	public final List<QuestChapter> chapters;
	private boolean invalid;
	public final Int2ObjectMap<QuestObject> objectMap;
	public final ConfigList<ConfigItemStack> emergencyItems;

	public QuestList(NBTTagCompound nbt)
	{
		super(0);
		chapters = new ArrayList<>();
		invalid = false;
		objectMap = new Int2ObjectOpenHashMap<>();
		objectMap.put(0, this);

		NBTTagList chapterList = nbt.getTagList("chapters", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < chapterList.tagCount(); i++)
		{
			QuestChapter chapter = new QuestChapter(this, chapterList.getCompoundTagAt(i));
			chapter.index = chapters.size();
			chapters.add(chapter);
			objectMap.put(chapter.id, chapter);
		}

		emergencyItems = new ConfigList<>(new ConfigItemStack(new ItemStack(Items.APPLE)));

		NBTTagList emergencyItemsList = nbt.getTagList("emergency_items", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < emergencyItemsList.tagCount(); i++)
		{
			ItemStack stack = new ItemStack(emergencyItemsList.getCompoundTagAt(i));

			if (!stack.isEmpty())
			{
				emergencyItems.add(new ConfigItemStack(stack));
			}
		}
	}

	@Override
	public QuestList getQuestList()
	{
		return this;
	}

	@Override
	public QuestObjectType getObjectType()
	{
		return QuestObjectType.LIST;
	}

	@Override
	public boolean isInvalid()
	{
		return invalid;
	}

	public void invalidate()
	{
		invalid = true;
		objectMap.clear();
	}

	@Override
	public int getProgress(IProgressData data)
	{
		int progress = 0;

		for (QuestChapter chapter : chapters)
		{
			progress += chapter.getProgress(data);
		}

		return progress;
	}

	@Override
	public int getMaxProgress()
	{
		int maxProgress = 0;

		for (QuestChapter chapter : chapters)
		{
			maxProgress += chapter.getMaxProgress();
		}

		return maxProgress;
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
	public void delete()
	{
	}

	@Nullable
	public QuestObject get(int id)
	{
		return id == 0 ? this : objectMap.get(id);
	}

	@Nullable
	public QuestChapter getChapter(int id)
	{
		QuestObject object = get(id);
		return object instanceof QuestChapter ? (QuestChapter) object : null;
	}

	@Nullable
	public Quest getQuest(int id)
	{
		QuestObject object = get(id);
		return object instanceof Quest ? (Quest) object : null;
	}

	@Nullable
	public QuestTask getTask(int id)
	{
		QuestObject object = get(id);
		return object instanceof QuestTask ? (QuestTask) object : null;
	}

	@Nullable
	public QuestReward getReward(int id)
	{
		QuestObject object = get(id);
		return object instanceof QuestReward ? (QuestReward) object : null;
	}

	public int requestID()
	{
		return 0;
	}

	public int getID(NBTTagCompound nbt)
	{
		int id = nbt.getInteger("id");

		if (id <= 0 || objectMap.containsKey(id))
		{
			id = requestID();
			nbt.setInteger("id", id);
		}

		return id;
	}

	@Nullable
	public QuestObject createAndAdd(QuestObjectType type, int parent, NBTTagCompound nbt)
	{
		switch (type)
		{
			case CHAPTER:
			{
				QuestChapter chapter = new QuestChapter(this, nbt);
				chapter.index = chapter.list.chapters.size();
				chapter.list.chapters.add(chapter);
				objectMap.put(chapter.id, chapter);
				return chapter;
			}
			case QUEST:
			{
				QuestChapter chapter = getChapter(parent);

				if (chapter != null)
				{
					Quest quest = new Quest(chapter, nbt);
					chapter.quests.add(quest);
					objectMap.put(quest.id, quest);
					return quest;
				}

				return null;
			}
			case TASK:
			{
				Quest quest = getQuest(parent);

				if (quest != null)
				{
					QuestTask task = QuestTasks.createTask(quest, nbt, false);
					quest.tasks.add(task);
					objectMap.put(task.id, task);

					for (IProgressData data : getAllData())
					{
						data.createTaskData(task);
					}

					return task;
				}

				return null;
			}
			case REWARD:
			{
				Quest quest = getQuest(parent);

				if (quest != null)
				{
					QuestReward reward = QuestRewards.createReward(quest, nbt, false);
					quest.rewards.add(reward);
					objectMap.put(reward.id, reward);
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
	public void writeData(NBTTagCompound nbt)
	{
		NBTTagList chaptersList = new NBTTagList();

		for (QuestChapter chapter : chapters)
		{
			NBTTagCompound chapterNBT = new NBTTagCompound();
			chapter.writeData(chapterNBT);
			chaptersList.appendTag(chapterNBT);
		}

		nbt.setTag("chapters", chaptersList);

		NBTTagList emergencyItemsList = new NBTTagList();

		for (ConfigItemStack value : emergencyItems)
		{
			emergencyItemsList.appendTag(value.getStack().serializeNBT());
		}

		nbt.setTag("emergency_items", emergencyItemsList);
	}

	@Nullable
	public abstract IProgressData getData(String owner);

	public abstract Collection<IProgressData> getAllData();

	@Override
	public Icon getIcon()
	{
		return GuiIcons.BOOK_RED;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentTranslation("ftbquests.list");
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		config.add("emergency_items", emergencyItems, new ConfigList<>(new ConfigItemStack(new ItemStack(Items.APPLE))));
	}
}