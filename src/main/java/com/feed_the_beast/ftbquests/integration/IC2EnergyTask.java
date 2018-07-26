package com.feed_the_beast.ftbquests.integration;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.block.TileQuest;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class IC2EnergyTask extends QuestTask
{
	public static final String ID = "ic2_energy";

	private final ConfigInt value;

	public IC2EnergyTask(Quest quest, NBTTagCompound nbt)
	{
		super(quest, nbt);
		value = new ConfigInt(nbt.getInteger("value"), 1, Integer.MAX_VALUE);
	}

	@Override
	public int getMaxProgress()
	{
		return value.getInt();
	}

	@Override
	public String getName()
	{
		return ID;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setInteger("value", value.getInt());
	}

	@Override
	public Icon getIcon()
	{
		return Icon.getIcon("item:ic2:te 1 74");
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentTranslation("ftbquests.task.ic2_energy.text", value);
	}

	@Override
	public TileQuest createCustomTileEntity(World world)
	{
		return new TileQuestIC2();
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("value", value, new ConfigInt(1));
	}

	@Override
	public QuestTaskData createData(IProgressData data)
	{
		return new Data(this, data);
	}

	public static class Data extends QuestTaskData<IC2EnergyTask>
	{
		private Data(IC2EnergyTask task, IProgressData data)
		{
			super(task, data);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
		{
			return false;
		}

		@Override
		@Nullable
		public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
		{
			return null;
		}

		public double injectEnergy(double amount)
		{
			if (amount > 0 && getProgress() < task.getMaxProgress())
			{
				int add = (int) Math.min(amount, task.getMaxProgress() - getProgress());

				if (add > 0)
				{
					setProgress(getProgress() + add, false);
					return amount - add;
				}
			}

			return amount;
		}
	}
}