package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.util.FileUtils;
import com.feed_the_beast.ftblib.lib.util.JsonUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsConfig;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class ServerQuestList extends QuestList
{
	public static ServerQuestList INSTANCE;

	public final File folder;

	public ServerQuestList(File f, List<String> errored)
	{
		folder = f;

		editing = FTBQuestsConfig.general.editing_mode;
		chapters.clear();

		Map<String, File> chapterFileMap = new HashMap<>();
		Map<ResourceLocation, File> questFileMap = new HashMap<>();

		File chapterFolders[] = null;

		JsonElement questsJson0 = JsonUtils.fromJson(new File(folder, "quests.json"));

		if (questsJson0.isJsonObject())
		{
			JsonObject questsJson = questsJson0.getAsJsonObject();

			if (questsJson.has("chapters"))
			{
				JsonArray a = questsJson.get("chapters").getAsJsonArray();
				chapterFolders = new File[a.size()];

				for (int i = 0; i < a.size(); i++)
				{
					chapterFolders[i] = new File(folder, a.get(i).getAsString());
				}
			}
		}

		if (chapterFolders == null || chapterFolders.length == 0)
		{
			return;
		}

		for (File chapterFolder : chapterFolders)
		{
			if (chapterFolder.exists() && chapterFolder.isDirectory())
			{
				File[] questFiles = chapterFolder.listFiles();

				if (questFiles != null && questFiles.length > 0)
				{
					QuestChapter chapter = new QuestChapter(this, chapterFolder.getName());

					for (File questFile : questFiles)
					{
						if (questFile.isFile() && !questFile.getName().equals("chapter.json") && questFile.getName().endsWith(".json"))
						{
							Quest quest = new Quest(chapter, FileUtils.getRawFileName(questFile));
							chapter.quests.put(quest.id.getResourcePath(), quest);
							questFileMap.put(quest.id, questFile);
						}
					}

					if (!chapter.quests.isEmpty())
					{
						chapters.put(chapter.getName(), chapter);
						chapterFileMap.put(chapter.getName(), new File(chapterFolder, "chapter.json"));
					}
				}
			}
		}

		for (QuestChapter chapter : chapters.values())
		{
			try
			{
				chapter.fromJson(JsonUtils.fromJson(chapterFileMap.get(chapter.getName())).getAsJsonObject());

				for (Quest quest : chapter.quests.values())
				{
					try
					{
						quest.fromJson(JsonUtils.fromJson(questFileMap.get(quest.id)));
					}
					catch (Exception ex)
					{
						String fn = quest.id.toString().replace(":", "/") + ".json";
						errored.add(fn);
						FTBQuests.LOGGER.error("Error loading " + fn + ": " + ex);

						/*if (CommonUtils.DEV_ENV)
						{
							ex.printStackTrace();
						}*/
					}
				}
			}
			catch (Exception ex)
			{
				String fn = chapter.getName() + "/chapter.json";
				errored.add(fn);
				FTBQuests.LOGGER.error("Error loading " + fn + ": " + ex);

				/*if (CommonUtils.DEV_ENV)
				{
					ex.printStackTrace();
				}*/
			}
		}
	}

	private JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty("editing", editing);

		saveAll = true;
		JsonArray chaptersJson = new JsonArray();

		for (QuestChapter chapter : chapters.values())
		{
			chaptersJson.add(chapter.getSerializableElement());
		}

		json.add("chapters", chaptersJson);
		saveAll = false;
		return json;
	}

	public void sendTo(EntityPlayerMP player)
	{
		new MessageSyncQuests(toJson()).sendTo(player);
	}

	public void sendToAll()
	{
		new MessageSyncQuests(toJson()).sendToAll();
	}

	public void saveQuestsFile()
	{
		JsonObject json = new JsonObject();

		if (!chapters.isEmpty())
		{
			JsonArray array = new JsonArray();

			for (QuestChapter chapter : chapters.values())
			{
				array.add(chapter.getName());
			}

			json.add("chapters", array);
		}

		JsonUtils.toJsonSafe(new File(folder, "quests.json"), json);
	}

	public void save(QuestChapter chapter)
	{
		JsonUtils.toJsonSafe(new File(folder, chapter.getName() + "/chapter.json"), chapter.getSerializableElement());
	}

	public void save(Quest quest)
	{
		JsonUtils.toJsonSafe(new File(folder, quest.chapter.getName() + "/" + quest.id.getResourcePath() + ".json"), quest.getSerializableElement());
	}
}