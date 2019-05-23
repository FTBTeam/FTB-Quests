package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.QuestFile;

/**
 * @author LatvianModder
 */
public class ClearFileCacheEvent extends FTBQuestsEvent
{
	private final QuestFile file;

	public ClearFileCacheEvent(QuestFile f)
	{
		file = f;
	}

	public QuestFile getFile()
	{
		return file;
	}
}