package com.feed_the_beast.ftbquests.quest.theme.selector;

import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;

/**
 * @author LatvianModder
 */
public class TypeSelector extends ThemeSelector
{
	public final QuestObjectType type;

	public TypeSelector(QuestObjectType t)
	{
		type = t;
	}

	@Override
	public boolean matches(QuestObjectBase object)
	{
		return object.getObjectType() == type;
	}

	@Override
	public ThemeSelectorType getType()
	{
		return ThemeSelectorType.TYPE;
	}

	@Override
	public int compareTo(ThemeSelector o)
	{
		if (o instanceof TypeSelector)
		{
			return ((TypeSelector) o).type.compareTo(type);
		}

		return super.compareTo(o);
	}

	@Override
	public String toString()
	{
		return type.id;
	}

	@Override
	public int hashCode()
	{
		return type.hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		else if (o instanceof TypeSelector)
		{
			return type == ((TypeSelector) o).type;
		}

		return false;
	}
}