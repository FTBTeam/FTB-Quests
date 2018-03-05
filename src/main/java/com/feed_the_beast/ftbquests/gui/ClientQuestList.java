package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestList;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author LatvianModder
 */
public class ClientQuestList extends QuestList implements IProgressData
{
	public static final ClientQuestList INSTANCE = new ClientQuestList();

	private ClientQuestList()
	{
	}

	public void fromJson(JsonObject json)
	{
		chapters.clear();
		editing = json.get("editing").getAsBoolean();

		JsonArray chaptersJson = json.get("chapters").getAsJsonArray();

		for (JsonElement element : chaptersJson)
		{
			JsonObject chapterJson = element.getAsJsonObject();
			QuestChapter chapter = new QuestChapter(this, chapterJson.get("id").getAsString());
			chapters.put(chapter.getName(), chapter);

			for (JsonElement element1 : chapterJson.get("quests").getAsJsonArray())
			{
				JsonObject questJson = element1.getAsJsonObject();
				Quest quest = new Quest(chapter, questJson.get("id").getAsString());
				chapter.quests.put(quest.id.getResourcePath(), quest);
			}
		}

		for (JsonElement element : chaptersJson)
		{
			JsonObject chapterJson = element.getAsJsonObject();
			QuestChapter chapter = chapters.get(chapterJson.get("id").getAsString());
			chapter.fromJson(chapterJson);

			for (JsonElement element1 : chapterJson.get("quests").getAsJsonArray())
			{
				JsonObject questJson = element1.getAsJsonObject();
				Quest quest = chapter.quests.get(questJson.get("id").getAsString());
				quest.fromJson(questJson);
			}
		}
	}

	@Override
	public int getQuestTaskProgress(QuestTask task)
	{
		return 0;
	}
}