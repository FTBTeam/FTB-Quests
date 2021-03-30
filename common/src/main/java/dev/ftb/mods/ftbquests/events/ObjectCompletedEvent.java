package dev.ftb.mods.ftbquests.events;

import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.PlayerData;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.task.Task;
import me.shedaniel.architectury.ForgeEvent;
import me.shedaniel.architectury.event.Actor;
import me.shedaniel.architectury.event.Event;
import me.shedaniel.architectury.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * @author LatvianModder
 */
@ForgeEvent
public class ObjectCompletedEvent<T extends QuestObject> {
	public static final Event<Actor<ObjectCompletedEvent>> GENERIC = EventFactory.createActorLoop();
	public static final Event<Actor<FileEvent>> FILE = EventFactory.createActorLoop();
	public static final Event<Actor<ChapterEvent>> CHAPTER = EventFactory.createActorLoop();
	public static final Event<Actor<QuestEvent>> QUEST = EventFactory.createActorLoop();
	public static final Event<Actor<TaskEvent>> TASK = EventFactory.createActorLoop();

	static {
		FILE.register(event -> GENERIC.invoker().act(event));
		CHAPTER.register(event -> GENERIC.invoker().act(event));
		QUEST.register(event -> GENERIC.invoker().act(event));
		TASK.register(event -> GENERIC.invoker().act(event));
	}

	private final PlayerData data;
	private final T object;
	private final List<ServerPlayer> onlineMembers;
	private final List<ServerPlayer> notifiedPlayers;

	private ObjectCompletedEvent(PlayerData t, T o, List<ServerPlayer> om, List<ServerPlayer> n) {
		data = t;
		object = o;
		onlineMembers = om;
		notifiedPlayers = n;
	}

	public boolean isCancelable() {
		return true;
	}

	public PlayerData getData() {
		return data;
	}

	public T getObject() {
		return object;
	}

	public List<ServerPlayer> getOnlineMembers() {
		return onlineMembers;
	}

	public List<ServerPlayer> getNotifiedPlayers() {
		return notifiedPlayers;
	}

	@Deprecated
	public PlayerData getTeam() {
		return getData();
	}

	public static class FileEvent extends ObjectCompletedEvent<QuestFile> {
		public FileEvent(PlayerData t, QuestFile o, List<ServerPlayer> om, List<ServerPlayer> n) {
			super(t, o, om, n);
		}

		public QuestFile getFile() {
			return getObject();
		}
	}

	public static class ChapterEvent extends ObjectCompletedEvent<Chapter> {
		public ChapterEvent(PlayerData t, Chapter o, List<ServerPlayer> om, List<ServerPlayer> n) {
			super(t, o, om, n);
		}

		public Chapter getChapter() {
			return getObject();
		}
	}

	public static class QuestEvent extends ObjectCompletedEvent<Quest> {
		public QuestEvent(PlayerData t, Quest o, List<ServerPlayer> om, List<ServerPlayer> n) {
			super(t, o, om, n);
		}

		public Quest getQuest() {
			return getObject();
		}
	}

	public static class TaskEvent extends ObjectCompletedEvent<Task> {
		public TaskEvent(PlayerData t, Task o, List<ServerPlayer> om, List<ServerPlayer> n) {
			super(t, o, om, n);
		}

		public Task getTask() {
			return getObject();
		}
	}
}