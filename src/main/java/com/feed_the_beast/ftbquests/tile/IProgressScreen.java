package com.feed_the_beast.ftbquests.tile;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public interface IProgressScreen extends IScreen
{
	@Nullable
	TileProgressScreenCore getScreen();
}