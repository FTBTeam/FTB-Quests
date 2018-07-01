package com.feed_the_beast.ftbquests.quest;

/**
 * @author LatvianModder
 */
public abstract class QuestObject
{
	public final int id;

	public QuestObject(int i)
	{
		id = i;
	}

	public abstract QuestList getQuestList();

	public boolean isInvalid()
	{
		return getQuestList().isInvalid();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + '#' + id;
	}

	@Override
	public final int hashCode()
	{
		return id;
	}

	@Override
	public final boolean equals(Object o)
	{
		return o == this || o instanceof QuestObject && id == o.hashCode();
	}
}