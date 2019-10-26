package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.task.Task;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;

/**
 * @author LatvianModder
 */
public class ObjectCompletedEvent<T extends QuestObject> extends FTBQuestsEvent
{
	private final QuestData data;
	private final T object;
	private final List<EntityPlayerMP> onlineMembers;
	private final List<EntityPlayerMP> notifiedPlayers;

	private ObjectCompletedEvent(QuestData t, T o, List<EntityPlayerMP> om, List<EntityPlayerMP> n)
	{
		data = t;
		object = o;
		onlineMembers = om;
		notifiedPlayers = n;
	}

	public QuestData getData()
	{
		return data;
	}

	public T getObject()
	{
		return object;
	}

	public List<EntityPlayerMP> getOnlineMembers()
	{
		return onlineMembers;
	}

	public List<EntityPlayerMP> getNotifiedPlayers()
	{
		return notifiedPlayers;
	}

	@Deprecated
	public QuestData getTeam()
	{
		return getData();
	}

	public static class FileEvent extends ObjectCompletedEvent<QuestFile>
	{
		public FileEvent(QuestData t, QuestFile o, List<EntityPlayerMP> om, List<EntityPlayerMP> n)
		{
			super(t, o, om, n);
		}

		public QuestFile getFile()
		{
			return getObject();
		}
	}

	public static class ChapterEvent extends ObjectCompletedEvent<Chapter>
	{
		public ChapterEvent(QuestData t, Chapter o, List<EntityPlayerMP> om, List<EntityPlayerMP> n)
		{
			super(t, o, om, n);
		}

		public Chapter getChapter()
		{
			return getObject();
		}
	}

	public static class QuestEvent extends ObjectCompletedEvent<Quest>
	{
		public QuestEvent(QuestData t, Quest o, List<EntityPlayerMP> om, List<EntityPlayerMP> n)
		{
			super(t, o, om, n);
		}

		public Quest getQuest()
		{
			return getObject();
		}
	}

	public static class TaskEvent extends ObjectCompletedEvent<Task>
	{
		public TaskEvent(QuestData t, Task o, List<EntityPlayerMP> om, List<EntityPlayerMP> n)
		{
			super(t, o, om, n);
		}

		public Task getTask()
		{
			return getObject();
		}
	}
}