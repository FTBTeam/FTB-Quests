package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.config.ConfigLong;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.nbt.NBTTagCompound;
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

	public final ConfigLong value;
	public final ConfigInt maxInput;

	public ForgeEnergyTask(Quest quest, NBTTagCompound nbt)
	{
		super(quest, nbt);
		value = new ConfigLong(nbt.getLong("value"), 1, Long.MAX_VALUE);
		maxInput = new ConfigInt(nbt.hasKey("max_input") ? nbt.getInteger("max_input") : 10000, 100, Integer.MAX_VALUE);
	}

	@Override
	public long getMaxProgress()
	{
		return value.getLong();
	}

	@Override
	public String getMaxProgressString()
	{
		return StringUtils.formatDouble(value.getDouble(), true);
	}

	@Override
	public String getName()
	{
		return ID;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setLong("value", value.getLong());
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
		return new TextComponentTranslation("ftbquests.task.forge_energy.text", StringUtils.formatDouble(value.getLong(), true));
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("value", value, new ConfigLong(1));
		group.add("max_input", maxInput, new ConfigInt(10000));
	}

	@Override
	public QuestTaskData createData(IProgressData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<ForgeEnergyTask> implements IEnergyStorage
	{
		private Data(ForgeEnergyTask task, IProgressData data)
		{
			super(task, data);
		}

		@Override
		public double getRelativeProgress()
		{
			return progress / (double) task.value.getLong();
		}

		@Override
		public String getProgressString()
		{
			return StringUtils.formatDouble(progress, true);
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
			if (maxReceive > 0 && progress < task.value.getLong())
			{
				long add = Math.min(task.maxInput.getInt(), Math.min(maxReceive, task.value.getLong() - progress));

				if (add > 0L)
				{
					if (!simulate)
					{
						progress += add;
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