package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.JsonUtils;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.google.gson.JsonObject;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class FluidTask extends QuestTask
{
	public final FluidStack fluid;
	private Icon icon = null;

	public FluidTask(Quest parent, int id, FluidStack fs)
	{
		super(parent, id);
		fluid = fs;
	}

	@Override
	public int getMaxProgress()
	{
		return fluid.amount;
	}

	@Override
	public Icon getIcon()
	{
		if (icon == null)
		{
			icon = Icon.getIcon(fluid.getFluid().getStill(fluid).toString());
		}

		return icon;
	}

	@Override
	public JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty("fluid", fluid.getFluid().getName());

		if (fluid.amount != 1000)
		{
			json.addProperty("amount", fluid.amount);
		}

		if (fluid.tag != null && !fluid.tag.hasNoTags())
		{
			json.add("nbt", JsonUtils.toJson(fluid.tag));
		}

		return json;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		StringBuilder builder = new StringBuilder();

		if (fluid.amount >= 1000)
		{
			if (fluid.amount % 1000 != 0)
			{
				builder.append(fluid.amount / 1000D);
			}
			else
			{
				builder.append(fluid.amount / 1000);
			}
		}
		else
		{
			builder.append(fluid.amount % 1000);
			builder.append('m');
		}

		builder.append("b of ");
		builder.append(fluid.getLocalizedName());
		return builder.toString();
	}

	@Override
	public QuestTaskData createData(IProgressData data)
	{
		return new Data(this, data);
	}

	private static class Data extends QuestTaskData<FluidTask> implements IFluidHandler
	{
		private final IFluidTankProperties[] properties;

		private Data(FluidTask t, IProgressData data)
		{
			super(t, data);
			properties = new IFluidTankProperties[1];
			properties[0] = new FluidTankProperties(task.fluid.copy(), task.fluid.amount, true, false);
		}

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
		{
			return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
		}

		@Nullable
		@Override
		public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
		{
			return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ? (T) this : null;
		}

		@Override
		public IFluidTankProperties[] getTankProperties()
		{
			if (properties[0].getContents() != null)
			{
				properties[0].getContents().amount = getProgress();
			}

			return properties;
		}

		@Override
		public int fill(FluidStack resource, boolean doFill)
		{
			if (getProgress() < task.fluid.amount && task.fluid.isFluidEqual(resource))
			{
				int add = Math.min(resource.amount, task.fluid.amount - getProgress());

				if (add > 0 && setProgress(getProgress() + add, !doFill))
				{
					return add;
				}
			}

			return 0;
		}

		@Nullable
		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain)
		{
			return null;
		}

		@Nullable
		@Override
		public FluidStack drain(int maxDrain, boolean doDrain)
		{
			return null;
		}
	}
}