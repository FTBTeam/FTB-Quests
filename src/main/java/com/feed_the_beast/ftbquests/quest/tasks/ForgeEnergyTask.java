package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ForgeEnergyTask extends QuestTask
{
	public final int energy;
	private Icon icon = null;

	public ForgeEnergyTask(Quest parent, int id, int e)
	{
		super(parent, id);
		energy = e;
	}

	@Override
	public int getMaxProgress()
	{
		return energy;
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
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setInteger("forge_energy", energy);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		return energy + " FE";
	}

	@Override
	public QuestTaskData createData(IProgressData data)
	{
		return new Data(this, data);
	}

	public static class Data extends QuestTaskData<ForgeEnergyTask> implements IEnergyStorage
	{
		private Data(ForgeEnergyTask task, IProgressData data)
		{
			super(task, data);
		}

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
		{
			return capability == CapabilityEnergy.ENERGY;
		}

		@Nullable
		@Override
		public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
		{
			return capability == CapabilityEnergy.ENERGY ? (T) this : null;
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate)
		{
			if (maxReceive > 0 && getProgress() < task.energy)
			{
				int add = Math.min(maxReceive, task.energy - getProgress());

				if (add > 0 && setProgress(getProgress() + add, simulate))
				{
					return add;
				}
			}

			return 0;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate)
		{
			return 0;
		}

		@Override
		public int getEnergyStored()
		{
			return 0;
		}

		@Override
		public int getMaxEnergyStored()
		{
			return task.energy;
		}

		@Override
		public boolean canExtract()
		{
			return false;
		}

		@Override
		public boolean canReceive()
		{
			return true;
		}
	}
}