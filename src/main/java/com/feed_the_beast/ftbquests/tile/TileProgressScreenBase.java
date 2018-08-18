package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.tile.TileBase;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public abstract class TileProgressScreenBase extends TileBase
{
	public int getOffsetX()
	{
		return 0;
	}

	public int getOffsetY()
	{
		return 0;
	}

	public int getOffsetZ()
	{
		return 0;
	}

	@Nullable
	public abstract TileProgressScreenCore getScreen();

	@Override
	protected boolean notifyBlock()
	{
		return !world.isRemote;
	}

	@Override
	public boolean canBeWrenched(EntityPlayer player)
	{
		return false;
	}
}