package com.feed_the_beast.ftbquests.quest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
public abstract class QuestList
{
	public final Map<String, QuestChapter> chapters;
	public boolean editing;
	public boolean saveAll;

	public QuestList()
	{
		chapters = new LinkedHashMap<>();
	}
}