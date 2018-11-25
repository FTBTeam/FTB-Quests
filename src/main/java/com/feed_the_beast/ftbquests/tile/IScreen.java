package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.tile.IItemWritableTile;

/**
 * @author LatvianModder
 */
public interface IScreen extends IItemWritableTile
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