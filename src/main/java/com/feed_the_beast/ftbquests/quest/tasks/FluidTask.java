package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.FTBQuestsItems;
import com.feed_the_beast.ftbquests.block.QuestBlockData;
import com.feed_the_beast.ftbquests.gui.ContainerFluidTask;
import com.feed_the_beast.ftbquests.gui.ContainerTaskBase;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class FluidTask extends QuestTask
{
	public static final String ID = "fluid";

	public final FluidStack fluid;
	public final int amount;
	private Icon icon = null;

	public FluidTask(Quest quest, int id, NBTTagCompound nbt)
	{
		super(quest, id);

		Fluid f = FluidRegistry.getFluid(nbt.getString("fluid"));

		if (f != null)
		{
			fluid = new FluidStack(f, 0, nbt.hasKey("nbt") ? nbt.getCompoundTag("nbt") : null);
		}
		else
		{
			fluid = null;
		}

		amount = nbt.hasKey("amount") ? nbt.getInteger("amount") : 1000;
	}

	@Override
	public boolean isInvalid()
	{
		return amount <= 0 || fluid == null || super.isInvalid();
	}

	@Override
	public int getMaxProgress()
	{
		return amount;
	}

	@Override
	public String getName()
	{
		return ID;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setString("type", "fluid");
		nbt.setString("fluid", fluid.getFluid().getName());

		if (amount != 1000)
		{
			nbt.setInteger("amount", amount);
		}

		if (fluid.tag != null && !fluid.tag.hasNoTags())
		{
			nbt.setTag("nbt", fluid.tag);
		}
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
	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		StringBuilder builder = new StringBuilder();

		if (amount >= 1000)
		{
			if (amount % 1000 != 0)
			{
				builder.append(amount / 1000D);
			}
			else
			{
				builder.append(amount / 1000);
			}
		}
		else
		{
			builder.append(amount % 1000);
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

	public static class Data extends QuestTaskData<FluidTask> implements IFluidHandlerItem, IItemHandler
	{
		private final IFluidTankProperties[] properties;
		private ItemStack outputStack = ItemStack.EMPTY;

		private Data(FluidTask t, IProgressData data)
		{
			super(t, data);
			properties = new IFluidTankProperties[1];
			properties[0] = new FluidTankProperties(task.fluid, task.amount, true, false);
		}

		@Override
		public void writeToNBT(NBTTagCompound nbt)
		{
			super.writeToNBT(nbt);

			if (!outputStack.isEmpty())
			{
				nbt.setTag("Output", outputStack.serializeNBT());
			}
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt)
		{
			super.readFromNBT(nbt);

			outputStack = nbt.hasKey("Output") ? new ItemStack(nbt.getCompoundTag("Output")) : ItemStack.EMPTY;
		}

		@Override
		public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
		{
			return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
					|| capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY
					|| capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
		}

		@Nullable
		@Override
		public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
		{
			return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
					|| capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY
					|| capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) this : null;
		}

		@Override
		public ContainerTaskBase getContainer(EntityPlayer player)
		{
			return new ContainerFluidTask(player, this);
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

		@Override
		public ItemStack getContainer()
		{
			ItemStack stack = new ItemStack(FTBQuestsItems.QUEST_BLOCK);
			QuestBlockData d = QuestBlockData.get(stack);

			if (d != null)
			{
				d.setTask(task.id);
				d.setOwner(data.getTeamID());
			}

			return stack;
		}

		@Override
		public int getSlots()
		{
			return 2;
		}

		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return slot == 0 ? ItemStack.EMPTY : outputStack;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			if (slot == 1 || stack.isEmpty())
			{
				return stack;
			}

			int progress = getProgress();
			int max = task.getMaxProgress();

			if (progress >= max)
			{
				return stack;
			}

			IFluidHandlerItem item = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

			if (item != null && outputStack.isEmpty())
			{
				FluidStack fluidStack = task.fluid.copy();
				fluidStack.amount = max - progress;
				fluidStack = item.drain(fluidStack, false);

				if (fluidStack == null || fluidStack.amount <= 0)
				{
					return stack;
				}

				if (!simulate)
				{
					item.drain(fluidStack, true);
					setProgress(progress + fluidStack.amount, false);
					outputStack = item.getContainer();
					data.syncTask(this);
				}

				return ItemStack.EMPTY;
			}

			return stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			if (slot == 0 || amount <= 0 || outputStack.isEmpty())
			{
				return ItemStack.EMPTY;
			}

			int taken = Math.min(amount, outputStack.getCount());

			ItemStack stack = ItemHandlerHelper.copyStackWithSize(outputStack, taken);

			if (!simulate)
			{
				outputStack.shrink(taken);

				if (outputStack.isEmpty())
				{
					outputStack = ItemStack.EMPTY;
				}

				data.syncTask(this);
			}

			return stack;
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return 1;
		}
	}
}