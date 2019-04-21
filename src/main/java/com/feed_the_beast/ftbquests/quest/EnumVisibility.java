package com.feed_the_beast.ftbquests.quest;

/**
 * @author LatvianModder
 */
public enum EnumVisibility
{
	VISIBLE(2),
	SECRET(1),
	INVISIBLE(0),
	INTERNAL(-1);

	public final int visibility;

	EnumVisibility(int v)
	{
		visibility = v;
	}

	public boolean isVisible()
	{
		return visibility >= VISIBLE.visibility;
	}

	public boolean isInvisible()
	{
		return visibility <= INVISIBLE.visibility;
	}

	public EnumVisibility strongest(EnumVisibility other)
	{
		return visibility <= other.visibility ? this : other;
	}
}