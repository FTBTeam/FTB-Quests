package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.task.Task;

/**
 * @author LatvianModder
 */
public class ObjectCompletedEvent<T extends QuestObject> extends FTBQuestsEvent
{
	private final QuestData team;
	protected final T object;

	private ObjectCompletedEvent(QuestData t, T o)
	{
		team = t;
		object = o;
	}

	public QuestData getTeam()
	{
		return team;
	}

	public static class FileEvent extends ObjectCompletedEvent<QuestFile>
	{
		public FileEvent(QuestData t, QuestFile o)
		{
			super(t, o);
		}

		public QuestFile getFile()
		{
			return object;
		}
	}

	public static class ChapterEvent extends ObjectCompletedEvent<Chapter>
	{
		public ChapterEvent(QuestData t, Chapter o)
		{
			super(t, o);
		}

		public Chapter getChapter()
		{
			return object;
		}
	}

	public static class QuestEvent extends ObjectCompletedEvent<Quest>
	{
		public QuestEvent(QuestData t, Quest o)
		{
			super(t, o);
		}

		public Quest getQuest()
		{
			return object;
		}
	}

	public static class TaskEvent extends ObjectCompletedEvent<Task>
	{
		public TaskEvent(QuestData t, Task o)
		{
			super(t, o);
		}

		public Task getTask()
		{
			return object;
		}
	}
}