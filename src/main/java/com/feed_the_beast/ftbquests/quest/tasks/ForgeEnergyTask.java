package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.client.resources.I18n;
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
	public static final String ID = "forge_energy";

	public final int value;

	public ForgeEnergyTask(Quest quest, int id, NBTTagCompound nbt)
	{
		super(quest, id);
		value = nbt.getInteger("value");
	}

	@Override
	public int getMaxProgress()
	{
		return value;
	}

	@Override
	public String getName()
	{
		return ID;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setInteger("value", value);
	}

	@Override
	public Icon getIcon()
	{
		return Icon.getIcon("minecraft:blocks/beacon");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		return I18n.format("ftbquests.gui.task.forge_energy", value);
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
			if (maxReceive > 0 && getProgress() < task.value)
			{
				int add = Math.min(maxReceive, task.value - getProgress());

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
			return task.value;
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