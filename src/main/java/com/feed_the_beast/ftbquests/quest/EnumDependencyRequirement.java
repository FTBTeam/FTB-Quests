package com.feed_the_beast.ftbquests.quest;

/**
 * @author LatvianModder
 */
public enum EnumDependencyRequirement
{
	ALL_COMPLETED(false, true),
	ONE_COMPLETED(true, true),
	ALL_STARTED(false, false),
	ONE_STARTED(true, false);

	public final boolean one;
	public final boolean completed;

	EnumDependencyRequirement(boolean o, boolean c)
	{
		one = o;
		completed = c;
	}
}