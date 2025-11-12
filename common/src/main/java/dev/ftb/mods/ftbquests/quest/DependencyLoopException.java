package dev.ftb.mods.ftbquests.quest;

public class DependencyLoopException extends RuntimeException {
	public final QuestObject object;

	public DependencyLoopException(QuestObject o) {
		object = o;
	}
}
