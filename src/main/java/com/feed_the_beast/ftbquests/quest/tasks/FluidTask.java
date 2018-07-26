package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.config.ConfigFluid;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.config.ConfigNBT;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
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

	public final ConfigFluid fluid;
	public final ConfigNBT fluidNBT;
	public final ConfigInt amount;

	public FluidTask(Quest quest, NBTTagCompound nbt)
	{
		super(quest, nbt);

		fluid = new ConfigFluid(FluidRegistry.getFluid(nbt.getString("fluid")), FluidRegistry.WATER);
		fluidNBT = new ConfigNBT(nbt.hasKey("nbt") ? nbt.getCompoundTag("nbt") : null);
		amount = new ConfigInt(nbt.hasKey("amount") ? nbt.getInteger("amount") : 1000, 1, Integer.MAX_VALUE);
	}

	@Override
	public boolean isInvalid()
	{
		return fluid.isEmpty() || super.isInvalid();
	}

	@Override
	public int getMaxProgress()
	{
		return amount.getInt();
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
		nbt.setString("fluid", fluid.getString());

		if (amount.getInt() != 1000)
		{
			nbt.setInteger("amount", amount.getInt());
		}

		if (fluidNBT.getNBT() != null)
		{
			nbt.setTag("nbt", fluidNBT.getNBT());
		}
	}

	@Override
	public Icon getIcon()
	{
		return Icon.getIcon(fluid.getFluid().getStill(createFluidStack(Fluid.BUCKET_VOLUME)).toString());
	}

	public FluidStack createFluidStack(int amount)
	{
		return new FluidStack(fluid.getFluid(), amount, fluidNBT.getNBT());
	}

	@Override
	public ITextComponent getDisplayName()
	{
		StringBuilder builder = new StringBuilder();
		int amount = getMaxProgress();

		if (amount >= Fluid.BUCKET_VOLUME)
		{
			if (amount % Fluid.BUCKET_VOLUME != 0)
			{
				builder.append(amount / (double) Fluid.BUCKET_VOLUME);
			}
			else
			{
				builder.append(amount / Fluid.BUCKET_VOLUME);
			}
		}
		else
		{
			builder.append(amount % Fluid.BUCKET_VOLUME);
			builder.append('m');
		}

		builder.append("b of ");
		return new TextComponentString(builder.toString()).appendSibling(fluid.getStringForGUI());
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("fluid", fluid, new ConfigFluid(FluidRegistry.WATER, FluidRegistry.WATER));
		group.add("fluid_nbt", fluidNBT, new ConfigNBT(null));
		group.add("amount", amount, new ConfigInt(1));
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
			properties[0] = new FluidTankProperties(task.createFluidStack(0), task.getMaxProgress(), true, false);
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
			FluidStack fluidStack = task.createFluidStack(Fluid.BUCKET_VOLUME);

			if (getProgress() < task.amount.getInt() && fluidStack.isFluidEqual(resource))
			{
				int add = Math.min(resource.amount, task.amount.getInt() - getProgress());

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
				FluidStack fluidStack = item.drain(task.createFluidStack(max - progress), false);

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