package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.config.ConfigDouble;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ForgeEnergyTask extends QuestTask
{
	public static final String ID = "forge_energy";

	public final ConfigDouble value;
	public final ConfigInt maxInput;

	public ForgeEnergyTask(Quest quest, NBTTagCompound nbt)
	{
		super(quest, nbt);
		value = new ConfigDouble(nbt.getLong("value"), 1, Double.POSITIVE_INFINITY);

		maxInput = new ConfigInt(nbt.hasKey("max_input") ? nbt.getInteger("max_input") : 10000, 100, Integer.MAX_VALUE);
	}

	@Override
	public int getMaxProgress()
	{
		return maxInput.getInt();
	}

	@Override
	public String getName()
	{
		return ID;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setLong("value", (long) value.getDouble());
		nbt.setInteger("max_input", value.getInt());
	}

	@Override
	public Icon getIcon()
	{
		return Icon.getIcon("minecraft:blocks/beacon");
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentTranslation("ftbquests.task.forge_energy.text", StringUtils.formatDouble(value.getDouble(), true));
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("value", value, new ConfigDouble(1));
		group.add("max_input", maxInput, new ConfigInt(10000));
	}

	@Override
	public QuestTaskData createData(IProgressData data)
	{
		return new Data(this, data);
	}

	public static class Data extends QuestTaskData<ForgeEnergyTask> implements IEnergyStorage
	{
		private long energy;

		private Data(ForgeEnergyTask task, IProgressData data)
		{
			super(task, data);
		}

		@Override
		public NBTBase toNBT()
		{
			return energy > 0 ? new NBTTagLong(energy) : null;
		}

		@Override
		public void fromNBT(@Nullable NBTBase nbt)
		{
			if (nbt instanceof NBTPrimitive)
			{
				energy = ((NBTPrimitive) nbt).getLong();
			}
			else
			{
				energy = 0L;
			}
		}

		@Override
		public int getProgress()
		{
			return (int) (getRelativeProgress() * 1000000D);
		}

		@Override
		public double getRelativeProgress()
		{
			return energy / task.value.getDouble();
		}

		@Override
		public String getProgressString()
		{
			return StringUtils.formatDouble(energy, true) + " / " + StringUtils.formatDouble(task.value.getDouble(), true);
		}

		@Override
		public void resetProgress()
		{
			energy = 0L;
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
			if (maxReceive > 0 && energy < task.value.getDouble())
			{
				long add = Math.min(task.maxInput.getInt(), Math.min(maxReceive, (long) (task.value.getDouble() - energy)));

				if (add > 0L)
				{
					if (!simulate)
					{
						energy += add;
						data.syncTask(this);
					}

					return (int) add;
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
			return task.maxInput.getInt();
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