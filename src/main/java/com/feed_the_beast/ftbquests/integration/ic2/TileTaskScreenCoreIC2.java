package com.feed_the_beast.ftbquests.integration.ic2;

import com.feed_the_beast.ftbquests.quest.task.TaskData;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenCore;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.util.EnumFacing;

/**
 * @author LatvianModder
 */
public class TileTaskScreenCoreIC2 extends TileTaskScreenCore implements IEnergySink
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
		TaskData d = getTaskData();

		if (d instanceof IC2EnergyTask.Data && d.task.quest.canStartTasks(d.data))
		{
			IC2EnergyTask.Data data = (IC2EnergyTask.Data) d;
			long e = data.task.value - data.progress;

			if (data.task.maxInput > 0L)
			{
				e = Math.min(data.task.maxInput, e);
			}

			return e;
		}

		return 0D;
	}

	@Override
	public int getSinkTier()
	{
		return 4;
	}

	@Override
	public double injectEnergy(EnumFacing facing, double amount, double voltage)
	{
		TaskData d = getTaskData();

		if (d instanceof IC2EnergyTask.Data && d.task.quest.canStartTasks(d.data))
		{
			return ((IC2EnergyTask.Data) d).injectEnergy(amount);
		}

		return amount;
	}
}