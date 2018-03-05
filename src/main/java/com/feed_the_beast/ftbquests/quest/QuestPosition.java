package com.feed_the_beast.ftbquests.quest;

/**
 * @author LatvianModder
 */
public final class QuestPosition
{
	public final int x, y;

	public QuestPosition(int _x, int _y)
	{
		x = _x;
		y = _y;
	}

	public int hashCode()
	{
		return x * 31 + y;
	}

	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		else if (obj instanceof QuestPosition)
		{
			QuestPosition p = (QuestPosition) obj;
			return x == p.x && y == p.y;
		}
		return false;
	}

	public String toString()
	{
		return "[" + x + ',' + y + ']';
	}
}