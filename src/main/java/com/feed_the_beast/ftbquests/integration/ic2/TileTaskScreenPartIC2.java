package com.feed_the_beast.ftbquests.integration.ic2;

import com.feed_the_beast.ftbquests.tile.TileTaskScreenCore;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenPart;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.util.EnumFacing;

/**
 * @author LatvianModder
 */
public class TileTaskScreenPartIC2 extends TileTaskScreenPart implements IEnergySink
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
		TileTaskScreenCore screen = getScreen();
		return screen instanceof TileTaskScreenCoreIC2 ? ((TileTaskScreenCoreIC2) screen).getDemandedEnergy() : 0D;
	}

	@Override
	public int getSinkTier()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public double injectEnergy(EnumFacing facing, double amount, double voltage)
	{
		TileTaskScreenCore screen = getScreen();
		return screen instanceof TileTaskScreenCoreIC2 ? ((TileTaskScreenCoreIC2) screen).injectEnergy(facing, amount, voltage) : amount;
	}
}