package com.feed_the_beast.ftbquests.integration.ic2;

import com.feed_the_beast.ftbquests.block.TileScreenCore;
import com.feed_the_beast.ftbquests.block.TileScreenPart;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
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

		if (screen == null)
		{
			return 0D;
		}

		QuestTaskData d = screen.getTaskData();
		return d == null ? 0D : d.task.getMaxProgress() - d.getProgress();
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

		if (screen == null)
		{
			return amount;
		}

		QuestTaskData d = screen.getTaskData();

		if (d instanceof IC2EnergyTask.Data)
		{
			return ((IC2EnergyTask.Data) d).injectEnergy(amount);
		}

		return amount;
	}
}