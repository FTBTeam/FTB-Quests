package com.feed_the_beast.ftbquests.integration.ic2;

import com.feed_the_beast.ftbquests.tile.TileScreenCore;
import com.feed_the_beast.ftbquests.tile.TileScreenPart;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.util.EnumFacing;

/**
 * @author LatvianModder
 */
public class TileScreenPartIC2 extends TileScreenPart implements IEnergySink
{
	@Override
	public void onLoad()
	{
		if (world != null && !world.isRemote)
		{
			EnergyNet.instance.addTile(this);
		}
	}

	@Override
	public void invalidate()
	{
		if (!isInvalid() && world != null && !world.isRemote)
		{
			EnergyNet.instance.removeTile(this);
		}

		super.invalidate();
	}

	@Override
	public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing facing)
	{
		return true;
	}

	@Override
	public double getDemandedEnergy()
	{
		TileScreenCore screen = getScreen();
		return screen instanceof TileScreenCoreIC2 ? ((TileScreenCoreIC2) screen).getDemandedEnergy() : 0D;
	}

	@Override
	public int getSinkTier()
	{
		return 4;
	}

	@Override
	public double injectEnergy(EnumFacing facing, double amount, double voltage)
	{
		TileScreenCore screen = getScreen();
		return screen instanceof TileScreenCoreIC2 ? ((TileScreenCoreIC2) screen).injectEnergy(facing, amount, voltage) : amount;
	}
}