package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.task.Task;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class ObjectCompletedEvent<T extends QuestObject> extends FTBQuestsEvent
{
	private final PlayerData data;
	private final T object;
	private final List<ServerPlayer> onlineMembers;
	private final List<ServerPlayer> notifiedPlayers;

	private ObjectCompletedEvent(PlayerData t, T o, List<ServerPlayer> om, List<ServerPlayer> n)
	{
		data = t;
		object = o;
		onlineMembers = om;
		notifiedPlayers = n;
	}

	public PlayerData getData()
	{
		return data;
	}

	public T getObject()
	{
		return object;
	}

	public List<ServerPlayer> getOnlineMembers()
	{
		return onlineMembers;
	}

	public List<ServerPlayer> getNotifiedPlayers()
	{
		return notifiedPlayers;
	}

	@Deprecated
	public PlayerData getTeam()
	{
		return getData();
	}

	public static class FileEvent extends ObjectCompletedEvent<QuestFile>
	{
		public FileEvent(PlayerData t, QuestFile o, List<ServerPlayer> om, List<ServerPlayer> n)
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
		public ChapterEvent(PlayerData t, Chapter o, List<ServerPlayer> om, List<ServerPlayer> n)
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
		public QuestEvent(PlayerData t, Quest o, List<ServerPlayer> om, List<ServerPlayer> n)
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
		public TaskEvent(PlayerData t, Task o, List<ServerPlayer> om, List<ServerPlayer> n)
		{
			super(t, o, om, n);
		}

		public Task getTask()
		{
			return getObject();
		}
	}
}