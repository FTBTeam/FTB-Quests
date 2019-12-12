package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigFluid;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigNBT;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.opengl.GL11;

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
	public void drawScreen(@Nullable TaskData data)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		Minecraft mc = Minecraft.getInstance();

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

			FluidStack stack = createFluidStack();
			TextureAtlasSprite sprite = mc.getTextureMap().getAtlasSprite(stack.getFluid().getAttributes().getStill(stack).toString());
			int color = stack.getFluid().getAttributes().getColor(stack);
			int alpha = (color >> 24) & 0xFF;
			int red = (color >> 16) & 0xFF;
			int green = (color >> 8) & 0xFF;
			int blue = color & 0xFF;
			double u0 = sprite.getMinU();
			double v0 = sprite.getMinV() + (sprite.getMaxV() - sprite.getMinV()) * (1D - r);
			double u1 = sprite.getMaxU();
			double v1 = sprite.getMaxV();

			mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
			mc.getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			buffer.pos(x, y + h, z).tex(u0, v1).color(red, green, blue, alpha).endVertex();
			buffer.pos(x + w, y + h, z).tex(u1, v1).color(red, green, blue, alpha).endVertex();
			buffer.pos(x + w, y, z).tex(u1, v0).color(red, green, blue, alpha).endVertex();
			buffer.pos(x, y, z).tex(u0, v0).color(red, green, blue, alpha).endVertex();
			tessellator.draw();
			mc.getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		}
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

		@Override
		public boolean isItemValid(int slot, ItemStack stack)
		{
			return true;
		}
	}

	public static class Data extends TaskData<FluidTask> implements IFluidHandler
	{
		private final LazyOptional<IFluidHandler> fluidHandlerProvider;
		private final LazyOptional<IItemHandler> itemHandlerProvider;

		private Data(FluidTask t, PlayerData data)
		{
			super(t, data);
			fluidHandlerProvider = LazyOptional.of(() -> this);
			itemHandlerProvider = LazyOptional.of(() -> this);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing)
		{
			if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			{
				return fluidHandlerProvider.cast();
			}
			else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			{
				return itemHandlerProvider.cast();
			}

			return LazyOptional.empty();
		}

		@Override
		public String getProgressString()
		{
			return getVolumeString((int) progress);
		}

		@Override
		public int getTanks()
		{
			return 1;
		}

		@Override
		public boolean isFluidValid(int tank, FluidStack stack)
		{
			return task.createFluidStack().isFluidEqual(stack);
		}

		@Override
		public int fill(FluidStack resource, FluidAction action)
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

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action)
		{
			return FluidStack.EMPTY;
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action)
		{
			return FluidStack.EMPTY;
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

		@Override
		public int getSlotLimit(int slot)
		{
			return 1;
		}
		*/

		@Override
		public FluidStack getFluidInTank(int tank)
		{
			return FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank)
		{
			return (int) Math.min(Integer.MAX_VALUE, task.amount);
		}
	}
}