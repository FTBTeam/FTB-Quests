package com.feed_the_beast.ftbquests.quest;

import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public final class Dependency
{
	public static final Predicate<Dependency> PREDICATE_INVALID = Dependency::isInvalid;

	public QuestObject object;
	public EnumDependencyType type = EnumDependencyType.REQUIRED;

	public boolean isInvalid()
	{
		return object == null || object.invalid || type == null;
	}

	public String toString()
	{
		return type.name + object;
	}

	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		else if (o instanceof Dependency)
		{
			Dependency d = (Dependency) o;
			return object == d.object && type == d.type;
		}

		return false;
	}

	public int hashCode()
	{
		return object.hashCode() * EnumDependencyType.NAME_MAP.size() + type.hashCode();
	}
}