package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.JsonUtils;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestList extends ProgressingQuestObject
{
	public final List<QuestChapter> chapters;
	private boolean invalid;
	private final Int2ObjectMap<QuestObject> objectMap;
	private int nextID;

	public QuestList(JsonObject json)
	{
		super(0);
		chapters = new ArrayList<>();
		invalid = false;
		objectMap = new Int2ObjectOpenHashMap<>();
		objectMap.put(0, this);
		nextID = json.has("next_id") ? json.get("next_id").getAsInt() : 1;

		if (nextID <= 0)
		{
			nextID = 1;
		}

		for (JsonElement element : json.has("chapters") ? json.get("chapters").getAsJsonArray() : Collections.<JsonElement>emptyList())
		{
			JsonObject chapterJson = element.getAsJsonObject();
			QuestChapter chapter = new QuestChapter(this, getID(chapterJson));
			chapters.add(chapter);
			objectMap.put(chapter.id, chapter);

			if (chapterJson.has("title"))
			{
				chapter.title = JsonUtils.deserializeTextComponent(chapterJson.get("title"));
			}

			if (chapterJson.has("description"))
			{
				if (chapterJson.get("description").isJsonArray())
				{
					for (JsonElement element1 : chapterJson.get("description").getAsJsonArray())
					{
						chapter.description.add(JsonUtils.deserializeTextComponent(element1));
					}
				}
				else
				{
					chapter.description.add(JsonUtils.deserializeTextComponent(chapterJson.get("description")));
				}
			}

			if (chapterJson.has("icon"))
			{
				chapter.icon = Icon.getIcon(chapterJson.get("icon"));
			}

			if (chapterJson.has("rewards"))
			{
				for (JsonElement element1 : chapterJson.get("rewards").getAsJsonArray())
				{
					JsonObject rewardJson = element1.getAsJsonObject();
					QuestReward reward = QuestReward.createReward(chapter, getID(rewardJson), rewardJson);

					if (reward != null)
					{
						chapter.rewards.add(reward);
						objectMap.put(reward.id, reward);
					}
				}
			}

			if (chapterJson.has("depends_on"))
			{
				for (JsonElement element1 : chapterJson.get("depends_on").getAsJsonArray())
				{
					chapter.dependencies.add(element1.getAsInt());
				}
			}

			for (JsonElement element1 : chapterJson.has("quests") ? chapterJson.get("quests").getAsJsonArray() : Collections.<JsonElement>emptyList())
			{
				JsonObject questJson = element1.getAsJsonObject();
				Quest quest = new Quest(chapter, getID(questJson));
				chapter.quests.add(quest);
				objectMap.put(quest.id, quest);

				quest.type = questJson.has("type") ? QuestType.NAME_MAP.get(questJson.get("type").getAsString()) : QuestType.NORMAL;
				quest.x = questJson.has("x") ? questJson.get("x").getAsInt() : 0;
				quest.y = questJson.has("y") ? questJson.get("y").getAsInt() : 0;
				quest.title = JsonUtils.deserializeTextComponent(questJson.get("title"));
				quest.description = JsonUtils.deserializeTextComponent(questJson.get("description"));
				quest.icon = questJson.has("icon") ? Icon.getIcon(questJson.get("icon")) : Icon.EMPTY;

				quest.text.clear();

				if (questJson.has("text"))
				{
					for (JsonElement element2 : questJson.get("text").getAsJsonArray())
					{
						quest.text.add(JsonUtils.deserializeTextComponent(element2));
					}
				}

				if (questJson.has("tasks"))
				{
					for (JsonElement element2 : questJson.get("tasks").getAsJsonArray())
					{
						JsonObject taskJson = element2.getAsJsonObject();
						QuestTask task = QuestTask.createTask(quest, getID(taskJson), taskJson);

						if (task != null)
						{
							quest.tasks.add(task);
							objectMap.put(task.id, task);
						}
					}
				}

				if (questJson.has("rewards"))
				{
					for (JsonElement element2 : questJson.get("rewards").getAsJsonArray())
					{
						JsonObject rewardJson = element2.getAsJsonObject();
						QuestReward reward = QuestReward.createReward(quest, getID(rewardJson), rewardJson);

						if (reward != null)
						{
							quest.rewards.add(reward);
							objectMap.put(reward.id, reward);
						}
					}
				}

				if (questJson.has("depends_on"))
				{
					for (JsonElement element2 : questJson.get("depends_on").getAsJsonArray())
					{
						quest.dependencies.add(element2.getAsInt());
					}
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
		int id;

		do
		{
			id = nextID++;
		}
		while (objectMap.containsKey(id));

		return id;
	}

	private int getID(JsonObject json)
	{
		int id = json.has("id") ? json.get("id").getAsInt() : 0;

		if (id <= 0 || objectMap.containsKey(id))
		{
			id = requestID();
			json.addProperty("id", id);
		}

		return id;
	}

	public final JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty("next_id", nextID);

		JsonArray chaptersJson = new JsonArray();

		for (QuestChapter chapter : chapters)
		{
			JsonObject chapterJson = new JsonObject();
			chapterJson.addProperty("id", chapter.id);
			chapterJson.add("title", JsonUtils.serializeTextComponent(chapter.title));

			if (!chapter.description.isEmpty())
			{
				JsonArray array = new JsonArray();

				for (ITextComponent component : chapter.description)
				{
					array.add(JsonUtils.serializeTextComponent(component));
				}

				chapterJson.add("description", array);
			}

			chapterJson.add("icon", chapter.icon.getJson());

			if (!chapter.dependencies.isEmpty())
			{
				JsonArray array = new JsonArray();

				for (int dependency : chapter.dependencies)
				{
					if (get(dependency) instanceof ProgressingQuestObject)
					{
						array.add(dependency);
					}
				}

				chapterJson.add("depends_on", array);
			}

			if (!chapter.rewards.isEmpty())
			{
				JsonArray array = new JsonArray();

				for (QuestReward reward : chapter.rewards)
				{
					JsonObject rewardJson = reward.toJson();
					rewardJson.addProperty("id", reward.id);
					array.add(rewardJson);
				}

				chapterJson.add("rewards", array);
			}

			if (!chapter.quests.isEmpty())
			{
				JsonArray questsJson = new JsonArray();

				for (Quest quest : chapter.quests)
				{
					JsonObject questJson = new JsonObject();

					questJson.addProperty("id", quest.id);

					if (quest.type != QuestType.NORMAL)
					{
						questJson.addProperty("type", quest.type.getName());
					}

					questJson.addProperty("x", quest.x);
					questJson.addProperty("y", quest.y);

					if (quest.title != null)
					{
						questJson.add("title", JsonUtils.serializeTextComponent(quest.title));
					}

					if (quest.description != null)
					{
						questJson.add("description", JsonUtils.serializeTextComponent(quest.description));
					}

					if (!quest.icon.isEmpty())
					{
						questJson.add("icon", quest.icon.getJson());
					}

					if (!quest.text.isEmpty())
					{
						JsonArray array = new JsonArray();

						for (ITextComponent c : quest.text)
						{
							array.add(JsonUtils.serializeTextComponent(c));
						}

						questJson.add("text", array);
					}

					if (!quest.dependencies.isEmpty())
					{
						JsonArray array = new JsonArray();

						for (int dependency : quest.dependencies)
						{
							if (get(dependency) instanceof ProgressingQuestObject)
							{
								array.add(dependency);
							}
						}

						questJson.add("depends_on", array);
					}

					if (!quest.tasks.isEmpty())
					{
						JsonArray array = new JsonArray();

						for (QuestTask task : quest.tasks)
						{
							JsonObject taskJson = task.toJson();
							taskJson.addProperty("id", task.id);
							array.add(taskJson);
						}

						questJson.add("tasks", array);
					}

					if (!quest.rewards.isEmpty())
					{
						JsonArray array = new JsonArray();

						for (QuestReward reward : quest.rewards)
						{
							JsonObject rewardJson = reward.toJson();
							rewardJson.addProperty("id", reward.id);
							array.add(rewardJson);
						}

						questJson.add("rewards", array);
					}

					questsJson.add(questJson);
				}

				chapterJson.add("quests", questsJson);
			}

			chaptersJson.add(chapterJson);
		}

		json.add("chapters", chaptersJson);
		return json;
	}
}