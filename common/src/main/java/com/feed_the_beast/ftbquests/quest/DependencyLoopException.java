package com.feed_the_beast.ftbquests.quest;

/**
 * @author LatvianModder
 */
public class DependencyLoopException extends RuntimeException {
	public final QuestObject object;

	public DependencyLoopException(QuestObject o) {
		object = o;
	}
}
