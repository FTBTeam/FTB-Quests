package com.feed_the_beast.ftbquests.integration;

import com.feed_the_beast.ftbquests.quest.tasks.QuestTasks;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.IEnergyNetEventReceiver;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.block.wiring.TileEntityElectricBlock;
import net.minecraft.nbt.NBTBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author LatvianModder
 */
public class IC2Integration implements IEnergyNetEventReceiver
{
	private final ArrayList<TileEntityElectricBlock> energyBlocks = new ArrayList<>();

	public void preInit()
	{
		MinecraftForge.EVENT_BUS.register(this);
		EnergyNet.instance.registerEventReceiver(this);

		CapabilityManager.INSTANCE.register(IIC2EnergyReceiver.class, new Capability.IStorage<IIC2EnergyReceiver>()
		{
			@Nullable
			@Override
			public NBTBase writeNBT(Capability<IIC2EnergyReceiver> capability, IIC2EnergyReceiver instance, EnumFacing side)
			{
				return null;
			}

			@Override
			public void readNBT(Capability<IIC2EnergyReceiver> capability, IIC2EnergyReceiver instance, EnumFacing side, NBTBase nbt)
			{
			}
		}, () -> null);

		QuestTasks.add(IC2EnergyTask.ID, IC2EnergyTask::new);
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END && !event.world.isRemote && event.world.provider.getDimension() == 0 && event.world.getTotalWorldTime() % 5L == 3L)
		{
			Iterator<TileEntityElectricBlock> iterator = energyBlocks.iterator();

			while (iterator.hasNext())
			{
				TileEntityElectricBlock tile = iterator.next();

				if (tile.isInvalid() || tile.getWorld() == null)
				{
					iterator.remove();
				}
				else if (tile.energy.getEnergy() > 0)
				{
					BlockPos pos = tile.getPos().offset(tile.getFacing());
					TileEntity tileEntity = tile.getWorld().getTileEntity(pos);
					IIC2EnergyReceiver receiver = tileEntity == null ? null : tileEntity.getCapability(IC2EnergyTask.CAP, null);

					if (receiver != null)
					{
						double r = Math.min(tile.getOutputEnergyUnitsPerTick() * 4D, receiver.receiveEnergy(tile.energy.getEnergy(), true));

						if (r > 0D)
						{
							tile.energy.useEnergy(r);
							receiver.receiveEnergy(r, false);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onWorldUnloaded(WorldEvent.Unload event)
	{
		energyBlocks.removeIf(tile -> tile.isInvalid() || tile.getWorld() == null || tile.getWorld() == event.getWorld());
	}

	@Override
	public void onAdd(IEnergyTile tile)
	{
		if (tile instanceof TileEntityElectricBlock)
		{
			energyBlocks.add((TileEntityElectricBlock) tile);
		}
	}

	@Override
	public void onRemove(IEnergyTile tile)
	{
		if (tile instanceof TileEntityElectricBlock)
		{
			energyBlocks.remove(tile);
		}
	}
}