package com.feed_the_beast.ftbquests.quest.theme.property;

/**
 * @author LatvianModder
 */
public class IntProperty extends ThemeProperty<Integer>
{
	public final int min;
	public final int max;

	public IntProperty(String n, int mn, int mx)
	{
		super(n);
		min = mn;
		max = mx;
	}

	public IntProperty(String n)
	{
		this(n, 0, Integer.MAX_VALUE);
	}

	@Override
	public Integer parse(String string)
	{
		try
		{
			int i = Integer.parseInt(string);

			if (i >= min && i <= max)
			{
				return i;
			}
		}
		catch (Exception ignored)
		{
		}

		return null;
	}
}