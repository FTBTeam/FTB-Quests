package com.feed_the_beast.ftbquests.tile;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public interface ITaskScreen extends IScreen
{
	@Nullable
	TileTaskScreenCore getScreen();
}