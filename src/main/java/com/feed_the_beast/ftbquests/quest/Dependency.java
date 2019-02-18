package com.feed_the_beast.ftbquests.quest;

import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class Dependency
{
	public static final Predicate<Dependency> PREDICATE_INVALID = Dependency::isInvalid;

	public QuestObject object;
	public EnumDependencyType type = EnumDependencyType.REQUIRED;

	public boolean isInvalid()
	{
		return object == null || object.invalid || type == null;
	}
}