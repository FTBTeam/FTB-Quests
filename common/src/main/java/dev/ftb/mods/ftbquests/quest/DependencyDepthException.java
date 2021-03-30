package dev.ftb.mods.ftbquests.quest;

/**
 * @author LatvianModder
 */
public class DependencyDepthException extends RuntimeException {
	public final QuestObject object;

	public DependencyDepthException(QuestObject o) {
		super("");
		object = o;
	}
}
