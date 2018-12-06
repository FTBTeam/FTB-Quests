package com.feed_the_beast.ftbquests.quest.task.filter;

import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;

/**
 * @author LatvianModder
 */
public enum NBTMatchingMode implements IWithID
{
	MATCH("match"),
	IGNORE("ignore"),
	CONTAIN("contain");

	public static final NameMap<NBTMatchingMode> NAME_MAP = NameMap.create(MATCH, values());

	private final String id;

	NBTMatchingMode(String i)
	{
		id = i;
	}

	@Override
	public String getID()
	{
		return id;
	}
}