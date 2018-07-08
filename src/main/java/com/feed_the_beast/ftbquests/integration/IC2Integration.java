package com.feed_the_beast.ftbquests.integration;

import com.feed_the_beast.ftbquests.events.QuestTaskEvent;
import ic2.core.block.wiring.TileEntityElectricBlock;
import net.minecraft.nbt.NBTBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author LatvianModder
 */
public class IC2Integration
{
	@CapabilityInject(IIC2EnergyReceiver.class)
	public static Capability<IIC2EnergyReceiver> CAP;

	private static final List<TileEntityElectricBlock> ENERGY_BLOCKS = new ArrayList<>();

	public static void preInit()
	{
		MinecraftForge.EVENT_BUS.register(IC2Integration.class);

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
	}

	@SubscribeEvent
	public static void onTileAttachCapabilitiesEvent(AttachCapabilitiesEvent<TileEntity> event)
	{
		if (event.getObject() instanceof TileEntityElectricBlock)
		{
			ENERGY_BLOCKS.add((TileEntityElectricBlock) event.getObject());
		}
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END && !event.world.isRemote && event.world.provider.getDimension() == 0)
		{
			Iterator<TileEntityElectricBlock> iterator = ENERGY_BLOCKS.iterator();

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
					IIC2EnergyReceiver receiver = tileEntity == null ? null : tile.getCapability(CAP, null);

					if (receiver != null)
					{
						double r = receiver.receiveEnergy(tile.energy.getEnergy(), true);

						if (r > 0D)
						{
							tile.energy.useEnergy(r);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onWorldUnloaded(WorldEvent.Unload event)
	{
		Iterator<TileEntityElectricBlock> iterator = ENERGY_BLOCKS.iterator();

		while (iterator.hasNext())
		{
			TileEntityElectricBlock tile = iterator.next();

			if (tile.isInvalid() || tile.getWorld() == null || tile.getWorld() == event.getWorld())
			{
				iterator.remove();
			}
		}
	}

	@SubscribeEvent
	public static void createQuestTask(QuestTaskEvent event)
	{
		if (event.getJson().has("ic2_power"))
		{
			event.setTask(new IC2EnergyTask(event.getParent(), event.getID(), event.getJson().get("ic2_power").getAsInt()));
		}
	}
}