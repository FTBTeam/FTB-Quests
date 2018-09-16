package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;

/**
 * @author LatvianModder
 */
public class ObjectCompletedEvent<T extends QuestObject> extends FTBQuestsEvent
{
	private final ITeamData team;
	protected final T object;

	private ObjectCompletedEvent(ITeamData t, T o)
	{
		team = t;
		object = o;
	}

	public ITeamData getTeam()
	{
		return team;
	}

	public static class FileEvent extends ObjectCompletedEvent<QuestFile>
	{
		public FileEvent(ITeamData t, QuestFile o)
		{
			super(t, o);
		}

		public QuestFile getFile()
		{
			return object;
		}
	}

	public static class ChapterEvent extends ObjectCompletedEvent<QuestChapter>
	{
		public ChapterEvent(ITeamData t, QuestChapter o)
		{
			super(t, o);
		}

		public QuestChapter getChapter()
		{
			return object;
		}
	}

	public static class QuestEvent extends ObjectCompletedEvent<Quest>
	{
		public QuestEvent(ITeamData t, Quest o)
		{
			super(t, o);
		}

		public Quest getQuest()
		{
			return object;
		}
	}

	public static class TaskEvent extends ObjectCompletedEvent<QuestTask>
	{
		public TaskEvent(ITeamData t, QuestTask o)
		{
			super(t, o);
		}

		public QuestTask getTask()
		{
			return object;
		}
	}
}