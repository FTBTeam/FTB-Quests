package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigFluid;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigNBT;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author LatvianModder
 */
public class FluidTask extends Task
{
	public static final ResourceLocation TANK_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/tank.png");

	public Fluid fluid = FluidRegistry.WATER;
	public NBTTagCompound fluidNBT = null;
	public int amount = Fluid.BUCKET_VOLUME;

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
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setString("fluid", fluid.getName());

		if (amount != Fluid.BUCKET_VOLUME)
		{
			nbt.setInteger("amount", amount);
		}

		if (fluidNBT != null)
		{
			nbt.setTag("nbt", fluidNBT);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);

		fluid = FluidRegistry.getFluid(nbt.getString("fluid"));

		if (fluid == null)
		{
			fluid = FluidRegistry.WATER;
		}

		fluidNBT = (NBTTagCompound) nbt.getTag("nbt");
		amount = nbt.hasKey("amount") ? (int) Math.min(Integer.MAX_VALUE, nbt.getLong("amount")) : Fluid.BUCKET_VOLUME;

		if (amount < 1)
		{
			amount = 1;
		}
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeString(fluid.getName());
		data.writeNBT(fluidNBT);
		data.writeVarInt(amount);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		fluid = FluidRegistry.getFluid(data.readString());

		if (fluid == null)
		{
			fluid = FluidRegistry.WATER;
		}

		fluidNBT = data.readNBT();
		amount = data.readVarInt();
	}

	public FluidStack createFluidStack(int amount)
	{
		return new FluidStack(fluid, amount, fluidNBT);
	}

	public static String getVolumeString(int a)
	{
		StringBuilder builder = new StringBuilder();

		if (a >= Fluid.BUCKET_VOLUME)
		{
			if (a % Fluid.BUCKET_VOLUME != 0L)
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
		}

		builder.append(' ');

		if (a < Fluid.BUCKET_VOLUME)
		{
			builder.append('m');
		}

		builder.append('B');
		return builder.toString();
	}

	@Override
	public Icon getAltIcon()
	{
		FluidStack fluidStack = createFluidStack(Fluid.BUCKET_VOLUME);
		return Icon.getIcon(fluidStack.getFluid().getStill(fluidStack).toString()).withTint(Color4I.rgb(fluidStack.getFluid().getColor(fluidStack))).combineWith(Icon.getIcon(FluidTask.TANK_TEXTURE.toString()));
	}

	@Override
	public String getAltTitle()
	{
		return getVolumeString(amount) + " of " + createFluidStack(Fluid.BUCKET_VOLUME).getLocalizedName();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);

		config.add("fluid", new ConfigFluid(FluidRegistry.WATER, FluidRegistry.WATER)
		{
			@Override
			public Fluid getFluid()
			{
				return fluid;
			}

			@Override
			public void setFluid(Fluid v)
			{
				fluid = v;
			}
		}, new ConfigFluid(FluidRegistry.WATER, FluidRegistry.WATER));

		config.add("fluid_nbt", new ConfigNBT(null)
		{
			@Override
			@Nullable
			public NBTTagCompound getNBT()
			{
				return fluidNBT;
			}

			@Override
			public void setNBT(@Nullable NBTTagCompound v)
			{
				fluidNBT = v;
			}
		}, new ConfigNBT(null));

		config.addInt("amount", () -> amount, v -> amount = v, Fluid.BUCKET_VOLUME, 1, Integer.MAX_VALUE);
	}

	@Override
	public boolean canInsertItem()
	{
		return true;
	}

	@Override
	public void drawScreen(@Nullable TaskData data)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		Minecraft mc = Minecraft.getMinecraft();

		mc.getTextureManager().bindTexture(TANK_TEXTURE);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		double x = -0.5;
		double y = -0.5;
		double w = 1;
		double h = 1;
		double z = 0;
		buffer.pos(x, y + h, z).tex(0, 1).endVertex();
		buffer.pos(x + w, y + h, z).tex(1, 1).endVertex();
		buffer.pos(x + w, y, z).tex(1, 0).endVertex();
		buffer.pos(x, y, z).tex(0, 0).endVertex();
		tessellator.draw();

		double r = data == null ? 0D : data.progress / (double) data.task.getMaxProgress();

		if (r > 0D)
		{
			x += 1D / 128D;
			w -= 1D / 64D;

			h = r * 30D / 32D;
			y = 1D / 32D + (1D - r) * 30D / 32D - 0.5;

			y -= 1D / 128D;
			h += 1D / 64D;
			z = 0.003D;

			FluidStack stack = createFluidStack(Fluid.BUCKET_VOLUME);
			TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(stack.getFluid().getStill(stack).toString());
			int color = stack.getFluid().getColor(stack);
			int alpha = (color >> 24) & 0xFF;
			int red = (color >> 16) & 0xFF;
			int green = (color >> 8) & 0xFF;
			int blue = color & 0xFF;
			double u0 = sprite.getMinU();
			double v0 = sprite.getMinV() + (sprite.getMaxV() - sprite.getMinV()) * (1D - r);
			double u1 = sprite.getMaxU();
			double v1 = sprite.getMaxV();

			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			buffer.pos(x, y + h, z).tex(u0, v1).color(red, green, blue, alpha).endVertex();
			buffer.pos(x + w, y + h, z).tex(u1, v1).color(red, green, blue, alpha).endVertex();
			buffer.pos(x + w, y, z).tex(u1, v0).color(red, green, blue, alpha).endVertex();
			buffer.pos(x, y, z).tex(u0, v0).color(red, green, blue, alpha).endVertex();
			tessellator.draw();
			mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		}
	}

	@Override
	@Nullable
	public Object getIngredient()
	{
		return createFluidStack(Fluid.BUCKET_VOLUME);
	}

	@Override
	public TaskData createData(QuestData data)
	{
		return new Data(this, data);
	}

	public enum ItemDestroyingInventory implements IItemHandler
	{
		INSTANCE;

		@Override
		public int getSlots()
		{
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return 64;
		}
	}

	public static class Data extends TaskData<FluidTask> implements IFluidHandler, IFluidTankProperties
	{
		private IFluidTankProperties[] properties;

		private Data(FluidTask t, QuestData data)
		{
			super(t, data);
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
		public String getProgressString()
		{
			return getVolumeString((int) progress);
		}

		@Override
		public IFluidTankProperties[] getTankProperties()
		{
			if (properties == null)
			{
				properties = new IFluidTankProperties[1];
				properties[0] = this;
			}

			return properties;
		}

		@Override
		public int fill(FluidStack resource, boolean doFill)
		{
			if (resource.amount > 0 && !isComplete() && task.fluid == resource.getFluid() && Objects.equals(task.fluidNBT, resource.tag) && task.quest.canStartTasks(data))
			{
				int add = (int) Math.min(resource.amount, task.amount - progress);

				if (add > 0)
				{
					if (doFill && !data.getFile().isClient())
					{
						addProgress(add);
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
		public ItemStack insertItem(ItemStack stack, boolean singleItem, boolean simulate, @Nullable EntityPlayer player)
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

		@Override
		public int getSlotLimit(int slot)
		{
			return 1;
		}

		@Nullable
		@Override
		public FluidStack getContents()
		{
			return task.createFluidStack((int) progress);
		}

		@Override
		public int getCapacity()
		{
			return task.amount;
		}

		@Override
		public boolean canFill()
		{
			return true;
		}

		@Override
		public boolean canDrain()
		{
			return false;
		}

		@Override
		public boolean canFillFluidType(FluidStack fluidStack)
		{
			return task.fluid == fluidStack.getFluid() && Objects.equals(task.fluidNBT, fluidStack.tag);
		}

		@Override
		public boolean canDrainFluidType(FluidStack fluidStack)
		{
			return false;
		}
	}
}