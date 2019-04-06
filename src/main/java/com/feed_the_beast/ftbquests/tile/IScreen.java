package com.feed_the_beast.ftbquests.tile;

import com.latmod.mods.itemfilters.api.IPaintable;

/**
 * @author LatvianModder
 */
public interface IScreen extends IPaintable
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