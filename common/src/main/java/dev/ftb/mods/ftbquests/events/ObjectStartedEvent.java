package dev.ftb.mods.ftbquests.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventActor;
import dev.architectury.event.EventFactory;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.task.Task;

/**
 * @author LatvianModder
 */
public class ObjectStartedEvent<T extends QuestObject> extends ObjectProgressEvent<T> {
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

	private ObjectStartedEvent(QuestProgressEventData<T> d) {
		super(d);
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