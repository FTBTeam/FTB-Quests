package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigFluid;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigNBT;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class FluidTask extends Task
{
	public static final ResourceLocation TANK_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/tank.png");

	public Fluid fluid = Fluids.WATER;
	public CompoundNBT fluidNBT = null;
	public long amount = FluidAttributes.BUCKET_VOLUME;

	private FluidStack cachedFluidStack = null;

	public FluidTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.FLUID;
	}

	@Override
	public long getMaxProgress()
	{
		return amount;
	}

	@Override
	public String getMaxProgressString()
	{
		return getVolumeString(amount);
	}

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putString("fluid", fluid.getRegistryName().toString());
		nbt.putLong("amount", amount);

		if (fluidNBT != null)
		{
			nbt.put("nbt", fluidNBT);
		}
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);

		fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(nbt.getString("fluid")));

		if (fluid == null || fluid == Fluids.EMPTY)
		{
			fluid = Fluids.WATER;
		}

		amount = Math.max(1L, nbt.getLong("amount"));
		fluidNBT = (CompoundNBT) nbt.get("nbt");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeResourceLocation(fluid.getRegistryName());
		buffer.writeCompoundTag(fluidNBT);
		buffer.writeVarLong(amount);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		fluid = ForgeRegistries.FLUIDS.getValue(buffer.readResourceLocation());

		if (fluid == null || fluid == Fluids.EMPTY)
		{
			fluid = Fluids.WATER;
		}

		fluidNBT = buffer.readCompoundTag();
		amount = buffer.readVarLong();
	}

	@Override
	public void clearCachedData()
	{
		super.clearCachedData();
		cachedFluidStack = null;
	}

	public FluidStack createFluidStack()
	{
		if (cachedFluidStack == null)
		{
			cachedFluidStack = new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME, fluidNBT);
		}

		return cachedFluidStack;
	}

	public static String getVolumeString(long a)
	{
		StringBuilder builder = new StringBuilder();

		if (a >= FluidAttributes.BUCKET_VOLUME)
		{
			if (a % FluidAttributes.BUCKET_VOLUME != 0L)
			{
				builder.append(a / (double) FluidAttributes.BUCKET_VOLUME);
			}
			else
			{
				builder.append(a / FluidAttributes.BUCKET_VOLUME);
			}
		}
		else
		{
			builder.append(a % FluidAttributes.BUCKET_VOLUME);
		}

		builder.append(' ');

		if (a < FluidAttributes.BUCKET_VOLUME)
		{
			builder.append('m');
		}

		builder.append('B');
		return builder.toString();
	}

	@Override
	public Icon getAltIcon()
	{
		FluidStack stack = createFluidStack();
		FluidAttributes a = stack.getFluid().getAttributes();
		return Icon.getIcon(a.getStill(stack).toString()).withTint(Color4I.rgb(a.getColor(stack))).combineWith(Icon.getIcon(FluidTask.TANK_TEXTURE.toString()));
	}

	@Override
	public String getAltTitle()
	{
		return getVolumeString(amount) + " of " + createFluidStack().getDisplayName().getFormattedText();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);

		config.add("fluid", new ConfigFluid(false), fluid, v -> fluid = v, Fluids.WATER);
		config.add("fluid_nbt", new ConfigNBT(), fluidNBT, v -> fluidNBT = v, null);
		config.addLong("amount", amount, v -> amount = v, FluidAttributes.BUCKET_VOLUME, 1, Long.MAX_VALUE);
	}

	@Override
	public boolean canInsertItem()
	{
		return true;
	}

	@Override
	@Nullable
	public Object getIngredient()
	{
		return createFluidStack();
	}

	@Override
	public TaskData createData(PlayerData data)
	{
		return new Data(this, data);
	}

	public static class Data extends TaskData<FluidTask>
	{
		private Data(FluidTask t, PlayerData data)
		{
			super(t, data);
		}

		@Override
		public String getProgressString()
		{
			return getVolumeString((int) progress);
		}

		public int fill(FluidStack resource, IFluidHandler.FluidAction action)
		{
			if (resource.getAmount() > 0 && !isComplete() && task.createFluidStack().isFluidEqual(resource) && data.canStartTasks(task.quest))
			{
				int add = (int) Math.min(resource.getAmount(), Math.min(Integer.MAX_VALUE, task.amount - progress));

				if (add > 0)
				{
					if (action.execute() && data.file.getSide().isServer())
					{
						addProgress(add);
					}

					return add;
				}
			}

			return 0;
		}
		
		/*
		@Override
		public ItemStack insertItem(ItemStack stack, boolean singleItem, boolean simulate, @Nullable PlayerEntity player)
		{
			if (isComplete())
			{
				return stack;
			}

			IFluidHandlerItem handlerItem = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

			if (handlerItem == null)
			{
				return stack;
			}

			FluidStack toDrain = task.createFluidStack(Fluid.BUCKET_VOLUME);
			FluidStack drainedFluid = handlerItem.drain(toDrain, false);

			if (drainedFluid == null || drainedFluid.amount <= 0)
			{
				return stack;
			}

			IItemHandler inv = player == null ? null : player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

			if (inv == null)
			{
				inv = ItemDestroyingInventory.INSTANCE;
			}

			ItemStack stack1 = stack.copy();
			FluidActionResult result = FluidUtil.tryFillContainerAndStow(stack1, this, inv, Integer.MAX_VALUE, player, !simulate);

			if (!result.isSuccess())
			{
				result = FluidUtil.tryEmptyContainerAndStow(stack1, this, inv, Integer.MAX_VALUE, player, !simulate);
			}

			if (result.isSuccess())
			{
				return player == null ? ItemStack.EMPTY : result.getResult();
			}

			return stack;
		}

		*/
	}
}