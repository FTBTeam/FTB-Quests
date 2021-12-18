package dev.ftb.mods.ftbquests.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventActor;
import dev.architectury.event.EventFactory;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.server.level.ServerPlayer;

import java.util.Date;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ObjectStartedEvent<T extends QuestObject> {
	public static final Event<EventActor<ObjectStartedEvent<?>>> GENERIC = EventFactory.createEventActorLoop();
	public static final Event<EventActor<FileEvent>> FILE = EventFactory.createEventActorLoop();
	public static final Event<EventActor<ChapterEvent>> CHAPTER = EventFactory.createEventActorLoop();
	public static final Event<EventActor<QuestEvent>> QUEST = EventFactory.createEventActorLoop();
	public static final Event<EventActor<TaskEvent>> TASK = EventFactory.createEventActorLoop();

	static {
		FILE.register(event -> GENERIC.invoker().act(event));
		CHAPTER.register(event -> GENERIC.invoker().act(event));
		QUEST.register(event -> GENERIC.invoker().act(event));
		TASK.register(event -> GENERIC.invoker().act(event));
	}

	private final QuestProgressEventData<T> data;

	private ObjectStartedEvent(QuestProgressEventData<T> d) {
		data = d;
	}

	public boolean isCancelable() {
		return true;
	}

	public Date getTime() {
		return data.time;
	}

	public TeamData getData() {
		return data.teamData;
	}

	public T getObject() {
		return data.object;
	}

	public List<ServerPlayer> getOnlineMembers() {
		return data.onlineMembers;
	}

	public List<ServerPlayer> getNotifiedPlayers() {
		return data.notifiedPlayers;
	}

	@Deprecated
	public TeamData getTeam() {
		return getData();
	}

	public static class FileEvent extends ObjectStartedEvent<QuestFile> {
		public FileEvent(QuestProgressEventData<QuestFile> d) {
			super(d);
		}

		public QuestFile getFile() {
			return getObject();
		}
	}

	public static class ChapterEvent extends ObjectStartedEvent<Chapter> {
		public ChapterEvent(QuestProgressEventData<Chapter> d) {
			super(d);
		}

		public Chapter getChapter() {
			return getObject();
		}
	}

	public static class QuestEvent extends ObjectStartedEvent<Quest> {
		public QuestEvent(QuestProgressEventData<Quest> d) {
			super(d);
		}

		public Quest getQuest() {
			return getObject();
		}
	}

	public static class TaskEvent extends ObjectStartedEvent<Task> {
		public TaskEvent(QuestProgressEventData<Task> d) {
			super(d);
		}

		public Task getTask() {
			return getObject();
		}
	}
}