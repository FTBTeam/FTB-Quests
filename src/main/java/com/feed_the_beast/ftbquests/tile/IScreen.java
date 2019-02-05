package com.feed_the_beast.ftbquests.tile;

/**
 * @author LatvianModder
 */
public interface IScreen
{
	default int getOffsetX()
	{
		return 0;
	}

	default int getOffsetY()
	{
		return 0;
	}

	default int getOffsetZ()
	{
		return 0;
	}
}