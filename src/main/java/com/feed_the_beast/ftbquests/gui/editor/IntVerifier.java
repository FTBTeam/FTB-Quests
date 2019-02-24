package com.feed_the_beast.ftbquests.gui.editor;

import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class IntVerifier implements Predicate<String>
{
	public static final IntVerifier DEFAULT = new IntVerifier(Integer.MIN_VALUE, Integer.MAX_VALUE);
	public static final IntVerifier NON_NEGATIVE = new IntVerifier(0, Integer.MAX_VALUE);
	public static final IntVerifier POSITIVE = new IntVerifier(1, Integer.MAX_VALUE);

	private final int min;
	private final int max;

	public IntVerifier(int mn, int mx)
	{
		min = mn;
		max = mx;
	}

	@Override
	public boolean test(String s)
	{
		if (s.isEmpty())
		{
			return false;
		}

		try
		{
			int i = Integer.decode(s);
			return i >= min && i <= max;
		}
		catch (NumberFormatException ex)
		{
			return false;
		}
	}
}
