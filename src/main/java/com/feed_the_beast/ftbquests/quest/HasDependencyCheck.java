package com.feed_the_beast.ftbquests.quest;

/**
 * @author LatvianModder
 */
public interface HasDependencyCheck
{
	boolean check(QuestObject depObject, QuestObject checkObject);
}