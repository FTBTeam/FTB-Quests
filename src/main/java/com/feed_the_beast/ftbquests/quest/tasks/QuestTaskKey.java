package com.feed_the_beast.ftbquests.quest.tasks;

import net.minecraft.util.ResourceLocation;

/**
 * @author LatvianModder
 */
public class QuestTaskKey
{
	public final ResourceLocation quest;
	public final int index;

	public QuestTaskKey(ResourceLocation q, int i)
	{
		quest = q;
		index = i;
	}

	public String toString()
	{
		return quest.toString() + ':' + index;
	}

	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		else if (o instanceof QuestTaskKey)
		{
			QuestTaskKey key = (QuestTaskKey) o;
			return index == key.index && quest.equals(key.quest);
		}
		return false;
	}

	public int hashCode()
	{
		return quest.hashCode();
	}
}