package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;

/**
 * @author LatvianModder
 */
public enum NumberMode implements IWithID
{
	EQUAL("=="),
	NOT("!="),
	GREATER_THAN(">"),
	GREATER_THAN_OR_EQUAL(">="),
	LESS_THAN("<"),
	LESS_THAN_OR_EQUAL("<=");

	public static final NameMap<NumberMode> NAME_MAP = NameMap.create(GREATER_THAN_OR_EQUAL, values());

	private final String name;

	NumberMode(String s)
	{
		name = s;
	}

	@Override
	public String getID()
	{
		return name;
	}

	public boolean check(int a, int b)
	{
		switch (this)
		{
			case EQUAL:
				return a == b;
			case NOT:
				return a != b;
			case GREATER_THAN:
				return a > b;
			case GREATER_THAN_OR_EQUAL:
				return a >= b;
			case LESS_THAN:
				return a < b;
			case LESS_THAN_OR_EQUAL:
				return a <= b;
			default:
				return false;
		}
	}
}