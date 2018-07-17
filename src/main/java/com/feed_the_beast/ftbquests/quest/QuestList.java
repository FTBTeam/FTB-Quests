package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.item.ItemStackSerializer;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.rewards.QuestRewards;
import com.feed_the_beast.ftbquests.quest.rewards.UnknownReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTasks;
import com.feed_the_beast.ftbquests.quest.tasks.UnknownTask;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
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
	public final List<ItemStack> emergencyChest;

	public QuestList(NBTTagCompound nbt)
	{
		super(0);
		chapters = new ArrayList<>();
		invalid = false;
		objectMap = new Int2ObjectOpenHashMap<>();
		objectMap.put(0, this);

		NBTTagList chaptersList = nbt.getTagList("chapters", Constants.NBT.TAG_COMPOUND);
		NBTTagList list;

		for (int i = 0; i < chaptersList.tagCount(); i++)
		{
			NBTTagCompound chapterNBT = chaptersList.getCompoundTagAt(i);
			QuestChapter chapter = new QuestChapter(this, getID(chapterNBT));
			chapters.add(chapter);
			objectMap.put(chapter.id, chapter);

			chapter.title = chapterNBT.getString("title");

			list = chapterNBT.getTagList("description", Constants.NBT.TAG_STRING);

			for (int j = 0; j < list.tagCount(); j++)
			{
				chapter.description.add(list.getStringTagAt(j));
			}

			chapter.icon = getIcon(chapterNBT);

			for (int d : chapterNBT.getIntArray("depends_on"))
			{
				chapter.dependencies.add(d);
			}

			NBTTagList questsList = chapterNBT.getTagList("quests", Constants.NBT.TAG_COMPOUND);

			for (int j = 0; j < questsList.tagCount(); j++)
			{
				NBTTagCompound questNBT = questsList.getCompoundTagAt(j);
				Quest quest = new Quest(chapter, getID(questNBT));
				chapter.quests.add(quest);
				objectMap.put(quest.id, quest);

				quest.type = QuestType.NAME_MAP.get(questNBT.getString("type"));
				quest.x = questNBT.getInteger("x");
				quest.y = questNBT.getInteger("y");
				quest.title = questNBT.getString("title");
				quest.description = questNBT.getString("description");
				quest.icon = getIcon(questNBT);

				list = questNBT.getTagList("text", Constants.NBT.TAG_STRING);

				for (int k = 0; k < list.tagCount(); k++)
				{
					quest.text.add(list.getStringTagAt(k));
				}

				list = questNBT.getTagList("tasks", Constants.NBT.TAG_COMPOUND);

				for (int k = 0; k < list.tagCount(); k++)
				{
					NBTTagCompound taskNBT = list.getCompoundTagAt(k);
					int id = getID(taskNBT);
					taskNBT.removeTag("id");
					QuestTask task = QuestTasks.createTask(quest, id, taskNBT);
					quest.tasks.add(task);
					objectMap.put(task.id, task);
				}

				list = questNBT.getTagList("rewards", Constants.NBT.TAG_COMPOUND);

				for (int k = 0; k < list.tagCount(); k++)
				{
					NBTTagCompound rewardNBT = list.getCompoundTagAt(k);
					int id = getID(rewardNBT);
					rewardNBT.removeTag("id");
					QuestReward reward = QuestRewards.createReward(quest, id, rewardNBT);
					quest.rewards.add(reward);
					objectMap.put(reward.id, reward);
				}

				for (int d : questNBT.getIntArray("depends_on"))
				{
					quest.dependencies.add(d);
				}
			}
		}

		emergencyChest = new ArrayList<>();

		NBTTagList emergencyChestList = nbt.getTagList("emergency_chest", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < emergencyChestList.tagCount(); i++)
		{
			ItemStack stack = new ItemStack(emergencyChestList.getCompoundTagAt(i));

			if (!stack.isEmpty())
			{
				emergencyChest.add(stack);
			}
		}
	}

	@Override
	public QuestList getQuestList()
	{
		return this;
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

	private int getID(NBTTagCompound nbt)
	{
		int id = nbt.getInteger("id");

		if (id <= 0 || objectMap.containsKey(id))
		{
			id = requestID();
			nbt.setInteger("id", id);
		}

		return id;
	}

	private ItemStack getIcon(NBTTagCompound nbt)
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

	public final NBTTagCompound toNBT()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList chaptersList = new NBTTagList();
		NBTTagList array;

		for (QuestChapter chapter : chapters)
		{
			NBTTagCompound chapterNBT = new NBTTagCompound();
			chapterNBT.setInteger("id", chapter.id);
			chapterNBT.setString("title", chapter.title);

			if (!chapter.description.isEmpty())
			{
				array = new NBTTagList();

				for (String s : chapter.description)
				{
					array.appendTag(new NBTTagString(s));
				}

				chapterNBT.setTag("description", array);
			}

			if (!chapter.icon.isEmpty())
			{
				chapterNBT.setTag("icon", chapter.icon.serializeNBT());
			}

			if (!chapter.dependencies.isEmpty())
			{
				chapterNBT.setIntArray("dependencies", chapter.dependencies.toIntArray());
			}

			if (!chapter.quests.isEmpty())
			{
				NBTTagList questsList = new NBTTagList();

				for (Quest quest : chapter.quests)
				{
					NBTTagCompound questNBT = new NBTTagCompound();

					questNBT.setInteger("id", quest.id);

					if (quest.type != QuestType.NORMAL)
					{
						questNBT.setString("type", quest.type.getName());
					}

					questNBT.setInteger("x", quest.x);
					questNBT.setInteger("y", quest.y);
					questNBT.setString("title", quest.title);

					if (!quest.description.isEmpty())
					{
						questNBT.setString("description", quest.description);
					}

					if (!quest.icon.isEmpty())
					{
						questNBT.setTag("icon", quest.icon.serializeNBT());
					}

					if (!quest.text.isEmpty())
					{
						array = new NBTTagList();

						for (String s : quest.text)
						{
							array.appendTag(new NBTTagString(s));
						}

						questNBT.setTag("text", array);
					}

					if (!quest.dependencies.isEmpty())
					{
						questNBT.setIntArray("dependencies", quest.dependencies.toIntArray());
					}

					if (!quest.tasks.isEmpty())
					{
						array = new NBTTagList();

						for (QuestTask task : quest.tasks)
						{
							NBTTagCompound taskNBT = new NBTTagCompound();
							task.writeData(taskNBT);
							taskNBT.setInteger("id", task.id);

							if (!(task instanceof UnknownTask))
							{
								taskNBT.setString("type", task.getName());
							}

							array.appendTag(taskNBT);
						}

						questNBT.setTag("tasks", array);
					}

					if (!quest.rewards.isEmpty())
					{
						array = new NBTTagList();

						for (QuestReward reward : quest.rewards)
						{
							NBTTagCompound rewardNBT = new NBTTagCompound();
							reward.writeData(rewardNBT);
							rewardNBT.setInteger("id", reward.id);

							if (!(reward instanceof UnknownReward))
							{
								rewardNBT.setString("type", reward.getName());
							}

							array.appendTag(rewardNBT);
						}

						questNBT.setTag("rewards", array);
					}

					questsList.appendTag(questNBT);
				}

				chapterNBT.setTag("quests", questsList);
			}

			chaptersList.appendTag(chapterNBT);
		}

		nbt.setTag("chapters", chaptersList);

		NBTTagList emergencyChestList = new NBTTagList();

		for (ItemStack stack : emergencyChest)
		{
			emergencyChestList.appendTag(stack.serializeNBT());
		}

		nbt.setTag("emergency_chest", emergencyChestList);
		return nbt;
	}

	@Nullable
	public abstract IProgressData getData(String owner);

	public abstract Collection<IProgressData> getAllData();
}