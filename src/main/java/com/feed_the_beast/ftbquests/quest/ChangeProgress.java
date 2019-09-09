package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.EnumTristate;
import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;

/**
 * @author LatvianModder
 */
public enum ChangeProgress implements IWithID
{
	RESET("reset", true, false),
	RESET_DEPS("reset_deps", true, true),
	COMPLETE("complete", false, false),
	COMPLETE_DEPS("complete_deps", false, true);

	public static final NameMap<ChangeProgress> NAME_MAP = NameMap.create(RESET, values());
	public static boolean sendUpdates = true;
	public static EnumTristate sendNotifications = EnumTristate.DEFAULT;

	private final String id;
	public final boolean reset, complete, dependencies;

	ChangeProgress(String n, boolean r, boolean d)
	{
		id = n;
		reset = r;
		complete = !r;
		dependencies = d;
	}

	@Override
	public String getId()
	{
		return id;
	}

	public String toString()
	{
		return id;
	}
}