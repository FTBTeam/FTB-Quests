package com.feed_the_beast.ftbquests.quest.theme.selector;

import com.feed_the_beast.ftbquests.quest.QuestObjectBase;

/**
 * @author LatvianModder
 */
public class IndirectParentSelector extends ThemeSelector
{
	public final ThemeSelector parent;
	public final ThemeSelector child;

	public IndirectParentSelector(ThemeSelector s, ThemeSelector c)
	{
		parent = s;
		child = c;
	}

	@Override
	public boolean matches(QuestObjectBase object)
	{
		if (!child.matches(object))
		{
			return false;
		}

		QuestObjectBase o;

		while (true)
		{
			o = object.getQuestFile().getBase(object.getParentID());

			if (o == null)
			{
				return false;
			}
			else if (parent.matches(o))
			{
				return true;
			}
		}
	}

	@Override
	public ThemeSelectorType getType()
	{
		return ThemeSelectorType.INDIRECT_PARENT;
	}

	@Override
	public String toString()
	{
		return parent + ">>" + child;
	}

	@Override
	public int hashCode()
	{
		return parent.hashCode() * 31 + child.hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		else if (o instanceof IndirectParentSelector)
		{
			IndirectParentSelector s = (IndirectParentSelector) o;
			return parent.equals(s.parent) && child.equals(s.child);
		}

		return false;
	}
}