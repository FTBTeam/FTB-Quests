package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.config.ConfigFluid;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.config.ConfigNBT;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.gui.ContainerFluidTask;
import com.feed_the_beast.ftbquests.gui.ContainerTaskBase;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
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
		FluidStack fluidStack = createFluidStack(Fluid.BUCKET_VOLUME);
		ItemStack stack = FluidUtil.getFilledBucket(fluidStack);

		if (stack.isEmpty())
		{
			return Icon.getIcon(fluid.getFluid().getStill(createFluidStack(Fluid.BUCKET_VOLUME)).toString());
		}
		else
		{
			return ItemIcon.getItemIcon(stack);
		}
	}

	public FluidStack createFluidStack(int amount)
	{
		return new FluidStack(fluid.getFluid(), amount, fluidNBT.getNBT());
	}

	@Override
	public ITextComponent getDisplayName()
	{
		StringBuilder builder = new StringBuilder();
		int a = amount.getInt();

		if (a >= Fluid.BUCKET_VOLUME)
		{
			if (a % Fluid.BUCKET_VOLUME != 0)
			{
				builder.append(a / (double) Fluid.BUCKET_VOLUME);
			}
			else
			{
				builder.append(a / Fluid.BUCKET_VOLUME);
			}
		}
		else
		{
			builder.append(a % Fluid.BUCKET_VOLUME);
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

	public static class Data extends QuestTaskData<FluidTask> implements IFluidHandler, IItemHandler
	{
		private final IFluidTankProperties[] properties;
		private ItemStack outputStack = ItemStack.EMPTY;
		private int progress;

		private Data(FluidTask t, IProgressData data)
		{
			super(t, data);
			properties = new IFluidTankProperties[1];
			properties[0] = new FluidTankProperties(task.createFluidStack(0), task.amount.getInt(), true, false);
		}

		@Nullable
		@Override
		public NBTBase toNBT()
		{
			if (outputStack.isEmpty())
			{
				return progress > 0 ? new NBTTagInt(progress) : null;
			}

			NBTTagCompound nbt = new NBTTagCompound();

			if (progress > 0)
			{
				nbt.setInteger("Progress", progress);
			}

			nbt.setTag("Output", outputStack.serializeNBT());
			return nbt;
		}

		@Override
		public void fromNBT(@Nullable NBTBase nbt)
		{
			if (nbt instanceof NBTPrimitive)
			{
				progress = ((NBTPrimitive) nbt).getInt();
				outputStack = ItemStack.EMPTY;
			}
			else if (nbt instanceof NBTTagCompound)
			{
				NBTTagCompound nbt1 = (NBTTagCompound) nbt;
				progress = nbt1.getInteger("Progress");
				outputStack = new ItemStack(nbt1.getCompoundTag("Output"));

				if (outputStack.isEmpty())
				{
					outputStack = ItemStack.EMPTY;
				}
			}
			else
			{
				progress = 0;
				outputStack = ItemStack.EMPTY;
			}
		}

		@Override
		public int getProgress()
		{
			return progress;
		}

		@Override
		public void resetProgress()
		{
			progress = 0;
		}

		@Override
		public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
		{
			return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
		}

		@Nullable
		@Override
		public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
		{
			return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) this : null;
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
				properties[0].getContents().amount = progress;
			}

			return properties;
		}

		@Override
		public int fill(FluidStack resource, boolean doFill)
		{
			FluidStack fluidStack = task.createFluidStack(Fluid.BUCKET_VOLUME);

			if (progress < task.amount.getInt() && fluidStack.isFluidEqual(resource))
			{
				int add = Math.min(resource.amount, task.amount.getInt() - progress);

				if (add > 0)
				{
					if (doFill)
					{
						progress += add;
						data.syncTask(this);
					}

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

			if (progress >= task.amount.getInt())
			{
				return stack;
			}

			IFluidHandlerItem item = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

			if (item != null && outputStack.isEmpty())
			{
				FluidStack fluidStack = item.drain(task.createFluidStack(task.amount.getInt() - progress), false);

				if (fluidStack == null || fluidStack.amount <= 0)
				{
					return stack;
				}

				if (!simulate)
				{
					item.drain(fluidStack, true);
					progress += fluidStack.amount;
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