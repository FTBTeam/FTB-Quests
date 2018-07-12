package com.feed_the_beast.ftbquests.integration;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class IC2EnergyTask extends QuestTask
{
	public static final String ID = "ic2_energy";

	@CapabilityInject(IIC2EnergyReceiver.class)
	public static Capability<IIC2EnergyReceiver> CAP;

	private final int energy;
	private Icon icon = null;

	public IC2EnergyTask(Quest quest, int id, NBTTagCompound nbt)
	{
		super(quest, id);
		energy = nbt.getInteger("value");
	}

	@Override
	public int getMaxProgress()
	{
		return energy;
	}

	@Override
	public String getName()
	{
		return ID;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setInteger("value", energy);
	}

	@Override
	public Icon getIcon()
	{
		if (icon == null)
		{
			icon = Icon.getIcon("minecraft:blocks/beacon");
		}

		return icon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		return energy + " EU";
	}

	@Override
	public QuestTaskData createData(IProgressData data)
	{
		return new Data(this, data);
	}

	public static class Data extends QuestTaskData<IC2EnergyTask> implements IIC2EnergyReceiver
	{
		private Data(IC2EnergyTask task, IProgressData data)
		{
			super(task, data);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
		{
			return capability == CAP;
		}

		@Override
		@Nullable
		public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
		{
			return capability == CAP ? (T) this : null;
		}

		@Override
		public double receiveEnergy(double maxReceive, boolean simulate)
		{
			if (maxReceive > 0 && getProgress() < task.energy)
			{
				int add = (int) Math.min(maxReceive, task.energy - getProgress());

				if (add > 0 && setProgress(getProgress() + add, simulate))
				{
					return add;
				}
			}

			return 0;
		}
	}
}