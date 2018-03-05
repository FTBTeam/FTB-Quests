package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.FinalIDObject;
import com.feed_the_beast.ftblib.lib.util.JsonUtils;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.IJsonSerializable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LatvianModder
 */
public final class QuestChapter extends FinalIDObject implements IJsonSerializable, IProgressing
{
	public final QuestList list;
	public ITextComponent title;
	public final List<ITextComponent> description;
	public Icon icon;
	public final Map<String, Quest> quests;
	public final List<QuestDependency> dependencies;
	public final List<QuestReward> rewards;

	public QuestChapter(QuestList l, String id)
	{
		super(id);
		list = l;
		title = new TextComponentString(id);
		description = new ArrayList<>();
		icon = Icon.EMPTY;
		quests = new LinkedHashMap<>();
		dependencies = new ArrayList<>();
		rewards = new ArrayList<>();
	}

	@Override
	public void fromJson(JsonElement json0)
	{
		JsonObject json = json0.getAsJsonObject();

		if (json.has("title"))
		{
			title = JsonUtils.deserializeTextComponent(json.get("title"));
		}

		if (json.has("description"))
		{
			for (JsonElement element1 : json.get("description").getAsJsonArray())
			{
				description.add(JsonUtils.deserializeTextComponent(element1));
			}
		}

		if (json.has("icon"))
		{
			icon = Icon.getIcon(json.get("icon"));
		}

		dependencies.clear();

		if (json.has("depends_on"))
		{
			for (JsonElement element : json.get("depends_on").getAsJsonArray())
			{
				if (element.isJsonPrimitive())
				{
					String[] s = element.getAsString().split(":", 2);
					QuestChapter chapter1 = list.chapters.get(s[0]);

					if (!equals(chapter1))
					{
						Quest quest1 = null;

						if (s.length == 2 && chapter1 != null && !s[1].equals("*"))
						{
							quest1 = chapter1.quests.get(s[1]);
						}

						dependencies.add(new QuestDependency(chapter1, quest1));
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

		if (list.saveAll)
		{
			json.addProperty("id", getName());
		}

		json.add("title", JsonUtils.serializeTextComponent(title));

		if (!description.isEmpty())
		{
			JsonArray array = new JsonArray();

			for (ITextComponent component : description)
			{
				array.add(JsonUtils.serializeTextComponent(component));
			}

			json.add("description", array);
		}

		json.add("icon", icon.getJson());

		if (!dependencies.isEmpty())
		{
			JsonArray array = new JsonArray();

			for (QuestDependency dependency : dependencies)
			{
				array.add(dependency.quest == null ? dependency.chapter.getName() : dependency.quest.id.toString());
			}

			json.add("depends_on", array);
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

		if (list.saveAll && !quests.isEmpty())
		{
			JsonArray questsJson = new JsonArray();

			for (Quest quest : quests.values())
			{
				questsJson.add(quest.getSerializableElement());
			}

			json.add("quests", questsJson);
		}

		return json;
	}

	@Override
	public int getProgress(IProgressData data)
	{
		int progress = 0;

		for (Quest quest : quests.values())
		{
			progress += quest.getProgress(data);
		}

		return progress;
	}

	@Override
	public int getMaxProgress()
	{
		int maxProgress = 0;

		for (Quest quest : quests.values())
		{
			maxProgress += quest.getMaxProgress();
		}

		return maxProgress;
	}
}