package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;

/**
 * @author LatvianModder
 */
public enum DependencyRequirement implements IWithID
{
	ALL_COMPLETED("all_completed", false, true),
	ONE_COMPLETED("one_completed", true, true),
	ALL_STARTED("all_started", false, false),
	ONE_STARTED("one_started", true, false);

	public static final NameMap<DependencyRequirement> NAME_MAP = NameMap.createWithBaseTranslationKey(ALL_COMPLETED, "ftbquests.quest.dependency_requirement", values());

	private final String id;
	public final boolean one;
	public final boolean completed;

	DependencyRequirement(String s, boolean o, boolean c)
	{
		id = s;
		one = o;
		completed = c;
	}

	@Override
	public String getId()
	{
		return id;
	}
}