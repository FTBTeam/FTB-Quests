package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskKey;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
public abstract class QuestList
{
	public final Map<String, QuestChapter> chapters;
	public boolean saveAll;

	public QuestList()
	{
		chapters = new LinkedHashMap<>();
	}

	@Nullable
	public Quest getQuest(ResourceLocation id)
	{
		QuestChapter c = chapters.get(id.getResourceDomain());
		return c == null ? null : c.quests.get(id.getResourcePath());
	}

	@Nullable
	public QuestTask getTask(QuestTaskKey key)
	{
		Quest quest = getQuest(key.quest);
		return quest == null || key.index < 0 || key.index >= quest.tasks.size() ? null : quest.tasks.get(key.index);
	}
}