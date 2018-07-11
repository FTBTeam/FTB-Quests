package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestList extends ProgressingQuestObject
{
	public final List<QuestChapter> chapters;
	private boolean invalid;
	private final Int2ObjectMap<QuestObject> objectMap;

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
			NBTTagCompound chapterTag = chaptersList.getCompoundTagAt(i);
			QuestChapter chapter = new QuestChapter(this, getID(chapterTag));
			chapters.add(chapter);
			objectMap.put(chapter.id, chapter);

			chapter.title = chapterTag.getString("title");

			list = chapterTag.getTagList("description", Constants.NBT.TAG_STRING);

			for (int j = 0; j < list.tagCount(); j++)
			{
				chapter.description.add(list.getStringTagAt(j));
			}

			chapter.icon = Icon.getIcon(chapterTag.getString("icon"));

			for (int d : chapterTag.getIntArray("depends_on"))
			{
				chapter.dependencies.add(d);
			}

			NBTTagList questsList = chapterTag.getTagList("quests", Constants.NBT.TAG_COMPOUND);

			for (int j = 0; j < questsList.tagCount(); j++)
			{
				NBTTagCompound questTag = questsList.getCompoundTagAt(j);
				Quest quest = new Quest(chapter, getID(questTag));
				chapter.quests.add(quest);
				objectMap.put(quest.id, quest);

				quest.type = QuestType.NAME_MAP.get(questTag.getString("type"));
				quest.x = questTag.getInteger("x");
				quest.y = questTag.getInteger("y");
				quest.title = questTag.getString("title");
				quest.description = questTag.getString("description");
				quest.icon = Icon.getIcon(questTag.getString("icon"));

				list = questTag.getTagList("text", Constants.NBT.TAG_STRING);

				for (int k = 0; k < list.tagCount(); k++)
				{
					quest.text.add(list.getStringTagAt(k));
				}

				list = questTag.getTagList("tasks", Constants.NBT.TAG_COMPOUND);

				for (int k = 0; k < list.tagCount(); k++)
				{
					NBTTagCompound taskJson = list.getCompoundTagAt(k);
					int id = getID(taskJson);
					taskJson.removeTag("id");
					QuestTask task = QuestTask.createTask(quest, id, taskJson);
					quest.tasks.add(task);
					objectMap.put(task.id, task);
				}

				list = questTag.getTagList("rewards", Constants.NBT.TAG_COMPOUND);

				for (int k = 0; k < list.tagCount(); k++)
				{
					NBTTagCompound rewardJson = list.getCompoundTagAt(k);
					int id = getID(rewardJson);
					rewardJson.removeTag("id");
					QuestReward reward = QuestReward.createReward(quest, id, rewardJson);

					if (reward != null)
					{
						quest.rewards.add(reward);
						objectMap.put(reward.id, reward);
					}
				}

				for (int d : questTag.getIntArray("depends_on"))
				{
					quest.dependencies.add(d);
				}
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

	public final NBTTagCompound toNBT()
	{
		NBTTagCompound json = new NBTTagCompound();
		NBTTagList chaptersJson = new NBTTagList();
		NBTTagList array;

		for (QuestChapter chapter : chapters)
		{
			NBTTagCompound chapterJson = new NBTTagCompound();
			chapterJson.setInteger("id", chapter.id);
			chapterJson.setString("title", chapter.title);

			if (!chapter.description.isEmpty())
			{
				array = new NBTTagList();

				for (String s : chapter.description)
				{
					array.appendTag(new NBTTagString(s));
				}

				chapterJson.setTag("description", array);
			}

			if (!chapter.icon.isEmpty())
			{
				chapterJson.setString("icon", chapter.icon.toString());
			}

			if (!chapter.dependencies.isEmpty())
			{
				chapterJson.setIntArray("dependencies", chapter.dependencies.toIntArray());
			}

			if (!chapter.quests.isEmpty())
			{
				NBTTagList questsJson = new NBTTagList();

				for (Quest quest : chapter.quests)
				{
					NBTTagCompound questJson = new NBTTagCompound();

					questJson.setInteger("id", quest.id);

					if (quest.type != QuestType.NORMAL)
					{
						questJson.setString("type", quest.type.getName());
					}

					questJson.setInteger("x", quest.x);
					questJson.setInteger("y", quest.y);
					questJson.setString("title", quest.title);

					if (!quest.description.isEmpty())
					{
						questJson.setString("description", quest.description);
					}

					if (!quest.icon.isEmpty())
					{
						questJson.setString("icon", quest.icon.toString());
					}

					if (!quest.text.isEmpty())
					{
						array = new NBTTagList();

						for (String s : quest.text)
						{
							array.appendTag(new NBTTagString(s));
						}

						questJson.setTag("text", array);
					}

					if (!quest.dependencies.isEmpty())
					{
						questJson.setIntArray("dependencies", quest.dependencies.toIntArray());
					}

					if (!quest.tasks.isEmpty())
					{
						array = new NBTTagList();

						for (QuestTask task : quest.tasks)
						{
							NBTTagCompound taskTag = new NBTTagCompound();
							task.writeData(taskTag);
							taskTag.setInteger("id", task.id);
							array.appendTag(taskTag);
						}

						questJson.setTag("tasks", array);
					}

					if (!quest.rewards.isEmpty())
					{
						array = new NBTTagList();

						for (QuestReward reward : quest.rewards)
						{
							NBTTagCompound rewardTag = new NBTTagCompound();
							reward.writeData(rewardTag);
							rewardTag.setInteger("id", reward.id);
							array.appendTag(rewardTag);
						}

						questJson.setTag("rewards", array);
					}

					questsJson.appendTag(questJson);
				}

				chapterJson.setTag("quests", questsJson);
			}

			chaptersJson.appendTag(chapterJson);
		}

		json.setTag("chapters", chaptersJson);
		return json;
	}
}