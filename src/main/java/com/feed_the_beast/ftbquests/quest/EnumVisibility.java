package com.feed_the_beast.ftbquests.quest;

/**
 * @author LatvianModder
 */
public enum EnumVisibility
{
	VISIBLE(2),
	SECRET(1),
	INVISIBLE(0);

	public final int visibility;

	EnumVisibility(int v)
	{
		visibility = v;
	}

	public boolean isVisible()
	{
		return visibility >= 2;
	}

	public boolean isInvisible()
	{
		return visibility <= 0;
	}

	public EnumVisibility strongest(EnumVisibility other)
	{
		return visibility <= other.visibility ? this : other;
	}
}