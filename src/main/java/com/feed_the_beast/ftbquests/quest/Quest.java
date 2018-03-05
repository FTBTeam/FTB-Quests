package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.JsonUtils;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.IJsonSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public final class Quest implements IJsonSerializable, IProgressing
{
	public final ResourceLocation id;
	public final QuestChapter chapter;
	public ITextComponent title;
	public ITextComponent description;
	public Icon icon;
	public QuestType type;
	public QuestPosition pos;
	public final List<ITextComponent> text;
	public final List<QuestDependency> dependencies;
	public final List<QuestTask> tasks;
	public final List<QuestReward> rewards;

	public Quest(QuestChapter c, String _id)
	{
		id = new ResourceLocation(c.getName(), StringUtils.getId(_id, StringUtils.FLAG_ID_DEFAULTS));
		chapter = c;
		title = new TextComponentString(id.getResourcePath());
		description = new TextComponentString("");
		icon = Icon.EMPTY;
		type = QuestType.NORMAL;
		pos = new QuestPosition(0, 0);
		text = new ArrayList<>();
		dependencies = new ArrayList<>();
		tasks = new ArrayList<>();
		rewards = new ArrayList<>();
	}

	@Override
	public String toString()
	{
		return id.toString();
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		return o == this || o instanceof Quest && id.equals(((Quest) o).id);
	}

	@Override
	public int getProgress(IProgressData data)
	{
		int progress = 0;

		for (QuestTask task : tasks)
		{
			progress += task.getProgress(data);
		}

		return progress;
	}

	@Override
	public int getMaxProgress()
	{
		int maxProgress = 0;

		for (QuestTask task : tasks)
		{
			maxProgress += task.getMaxProgress();
		}

		return maxProgress;
	}

	public boolean isVisible(IProgressData data)
	{
		switch (type)
		{
			case SECRET:
				for (QuestDependency d : dependencies)
				{
					if (d.isComplete(data))
					{
						return true;
					}
				}

				return false;
			case INVISIBLE:
				for (QuestDependency d : dependencies)
				{
					if (!d.isComplete(data))
					{
						return false;
					}
				}

				return true;
			default:
				return true;
		}
	}

	@Override
	public void fromJson(JsonElement json0)
	{
		JsonObject json = json0.getAsJsonObject();
		type = json.has("type") ? QuestType.NAME_MAP.get(json.get("type").getAsString()) : QuestType.NORMAL;
		int x = json.has("x") ? json.get("x").getAsInt() : 0;

		if (x < 0)
		{
			throw new IllegalArgumentException("x can't be smaller than 0!");
		}

		int y = json.has("y") ? json.get("y").getAsInt() : 0;

		if (y < 0)
		{
			throw new IllegalArgumentException("y can't be smaller than 0!");
		}

		pos = new QuestPosition(x, y);
		title = json.has("title") ? JsonUtils.deserializeTextComponent(json.get("title")) : new TextComponentString(id.toString());
		description = json.has("description") ? JsonUtils.deserializeTextComponent(json.get("description")) : new TextComponentString("");
		icon = json.has("icon") ? Icon.getIcon(json.get("icon")) : Icon.EMPTY;

		text.clear();

		if (json.has("text"))
		{
			for (JsonElement element : json.get("text").getAsJsonArray())
			{
				text.add(JsonUtils.deserializeTextComponent(element));
			}
		}

		dependencies.clear();

		if (json.has("depends_on"))
		{
			for (JsonElement element : json.get("depends_on").getAsJsonArray())
			{
				if (element.isJsonPrimitive())
				{
					String[] s = element.getAsString().split(":", 2);
					QuestChapter chapter1 = null;
					Quest quest1 = null;

					if (s.length == 1)
					{
						quest1 = chapter.quests.get(s[0]);

						if (quest1 != null)
						{
							chapter1 = quest1.chapter;
						}
					}
					else
					{
						chapter1 = chapter.list.chapters.get(s[0]);

						if (chapter1 != null && !s[1].equals("*"))
						{
							quest1 = chapter1.quests.get(s[1]);
						}
					}

					if (!equals(quest1))
					{
						dependencies.add(new QuestDependency(chapter1, quest1));
					}
				}
			}
		}

		tasks.clear();

		if (json.has("tasks"))
		{
			for (JsonElement element : json.get("tasks").getAsJsonArray())
			{
				if (element.isJsonObject())
				{
					QuestTask task = QuestTask.createTask(this, tasks.size(), element);

					if (task != null)
					{
						tasks.add(task);
					}
				}
			}
		}

		rewards.clear();

		if (json.has("rewards"))
		{
			for (JsonElement element : json.get("rewards").getAsJsonArray())
			{
				if (element.isJsonObject())
				{
					QuestReward reward = QuestReward.createReward(element);

					if (reward != null)
					{
						rewards.add(reward);
					}
				}
			}
		}
	}

	@Override
	public JsonElement getSerializableElement()
	{
		JsonObject json = new JsonObject();

		if (chapter.list.saveAll)
		{
			json.addProperty("id", id.getResourcePath());
		}

		if (type != QuestType.NORMAL)
		{
			json.addProperty("type", type.getName());
		}

		json.addProperty("x", pos.x);
		json.addProperty("y", pos.y);
		json.add("title", JsonUtils.serializeTextComponent(title));
		json.add("description", JsonUtils.serializeTextComponent(description));
		json.add("icon", icon.getJson());

		if (!text.isEmpty())
		{
			JsonArray array = new JsonArray();

			for (ITextComponent c : text)
			{
				array.add(JsonUtils.serializeTextComponent(c));
			}

			json.add("text", array);
		}

		if (!dependencies.isEmpty())
		{
			JsonArray array = new JsonArray();

			for (QuestDependency dependency : dependencies)
			{
				array.add(dependency.quest == null ? dependency.chapter.getName() + ":*" : chapter.equals(dependency.chapter) ? dependency.quest.id.getResourcePath() : dependency.quest.toString());
			}

			json.add("depends_on", array);
		}

		if (!tasks.isEmpty())
		{
			JsonArray array = new JsonArray();

			for (QuestTask task : tasks)
			{
				array.add(task.toJson());
			}

			json.add("tasks", array);
		}

		if (!rewards.isEmpty())
		{
			JsonArray array = new JsonArray();

			for (QuestReward reward : rewards)
			{
				array.add(reward.toJson());
			}

			json.add("rewards", array);
		}

		return json;
	}
}