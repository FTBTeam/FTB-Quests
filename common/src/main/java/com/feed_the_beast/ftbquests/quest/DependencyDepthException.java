package com.feed_the_beast.ftbquests.quest;

/**
 * @author LatvianModder
 */
public class DependencyDepthException extends RuntimeException
{
	public final QuestObject object;

	public DependencyDepthException(QuestObject o)
	{
		super("");
		object = o;
	}
}
