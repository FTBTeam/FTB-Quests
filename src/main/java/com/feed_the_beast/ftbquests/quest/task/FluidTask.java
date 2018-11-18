package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigFluid;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigNBT;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class FluidTask extends QuestTask
{
	public static final ResourceLocation TANK_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/tank.png");

	public Fluid fluid = FluidRegistry.WATER;
	public NBTTagCompound fluidNBT = null;
	public long amount = 1000;

	public FluidTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public QuestTaskType getType()
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

		if (amount != 1000)
		{
			nbt.setLong("amount", amount);
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
		amount = nbt.hasKey("amount") ? nbt.getLong("amount") : 1000;

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
		data.writeVarLong(amount);
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
		amount = data.readVarLong();
	}

	public FluidStack createFluidStack(int amount)
	{
		return new FluidStack(fluid, amount, fluidNBT);
	}

	public static String getVolumeString(long a)
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
	public ITextComponent getAltDisplayName()
	{
		return new TextComponentString(getVolumeString(amount)).appendText(" of ").appendText(createFluidStack(1000).getLocalizedName());
	}

	@Override
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

		config.addLong("amount", () -> amount, v -> amount = v, 1000L, 1, Long.MAX_VALUE);
	}

	@Override
	public boolean canInsertItem()
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawGUI(@Nullable QuestTaskData data, int x, int y, int w, int h)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		double r = data == null ? 0D : data.getRelativeProgress() / 100D;

		if (r > 0D)
		{
			double h1 = (r * 30D / 32D) * h;
			double y1 = y + (1D / 32D + (1D - r) * 30D / 32D) * h;

			FluidStack stack = createFluidStack(Fluid.BUCKET_VOLUME);
			TextureAtlasSprite sprite = ClientUtils.MC.getTextureMapBlocks().getAtlasSprite(stack.getFluid().getStill(stack).toString());
			int color = stack.getFluid().getColor(stack);
			int alpha = (color >> 24) & 0xFF;
			int red = (color >> 16) & 0xFF;
			int green = (color >> 8) & 0xFF;
			int blue = color & 0xFF;
			double u0 = sprite.getMinU();
			double v0 = sprite.getMinV() + (sprite.getMaxV() - sprite.getMinV()) * (1D - r);
			double u1 = sprite.getMaxU();
			double v1 = sprite.getMaxV();

			ClientUtils.MC.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			ClientUtils.MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			buffer.pos(x, y1 + h1, 0).tex(u0, v1).color(red, green, blue, alpha).endVertex();
			buffer.pos(x + w, y1 + h1, 0).tex(u1, v1).color(red, green, blue, alpha).endVertex();
			buffer.pos(x + w, y1, 0).tex(u1, v0).color(red, green, blue, alpha).endVertex();
			buffer.pos(x, y1, 0).tex(u0, v0).color(red, green, blue, alpha).endVertex();
			tessellator.draw();
			ClientUtils.MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		}

		ClientUtils.MC.getTextureManager().bindTexture(TANK_TEXTURE);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x, y + h, 0).tex(0, 1).endVertex();
		buffer.pos(x + w, y + h, 0).tex(1, 1).endVertex();
		buffer.pos(x + w, y, 0).tex(1, 0).endVertex();
		buffer.pos(x, y, 0).tex(0, 0).endVertex();
		tessellator.draw();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawScreen(@Nullable QuestTaskData data)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		ClientUtils.MC.getTextureManager().bindTexture(TANK_TEXTURE);
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

		double r = data == null ? 0D : data.getRelativeProgress() / 100D;

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
			TextureAtlasSprite sprite = ClientUtils.MC.getTextureMapBlocks().getAtlasSprite(stack.getFluid().getStill(stack).toString());
			int color = stack.getFluid().getColor(stack);
			int alpha = (color >> 24) & 0xFF;
			int red = (color >> 16) & 0xFF;
			int green = (color >> 8) & 0xFF;
			int blue = color & 0xFF;
			double u0 = sprite.getMinU();
			double v0 = sprite.getMinV() + (sprite.getMaxV() - sprite.getMinV()) * (1D - r);
			double u1 = sprite.getMaxU();
			double v1 = sprite.getMaxV();

			ClientUtils.MC.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			ClientUtils.MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			buffer.pos(x, y + h, z).tex(u0, v1).color(red, green, blue, alpha).endVertex();
			buffer.pos(x + w, y + h, z).tex(u1, v1).color(red, green, blue, alpha).endVertex();
			buffer.pos(x + w, y, z).tex(u1, v0).color(red, green, blue, alpha).endVertex();
			buffer.pos(x, y, z).tex(u0, v0).color(red, green, blue, alpha).endVertex();
			tessellator.draw();
			ClientUtils.MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		}
	}

	@Override
	public QuestTaskData createData(ITeamData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<FluidTask> implements IFluidHandler
	{
		private final IFluidTankProperties[] properties;

		private Data(FluidTask t, ITeamData data)
		{
			super(t, data);
			properties = new IFluidTankProperties[1];
			properties[0] = new FluidTankProperties(task.createFluidStack(0), 0, true, false);
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
			return getVolumeString(progress);
		}

		@Override
		public IFluidTankProperties[] getTankProperties()
		{
			return properties;
		}

		@Override
		public int fill(FluidStack resource, boolean doFill)
		{
			FluidStack fluidStack = task.createFluidStack(Fluid.BUCKET_VOLUME);

			if (progress < task.amount && fluidStack.isFluidEqual(resource))
			{
				long add = Math.min(100000000000L, Math.min(resource.amount, task.amount - progress));

				if (add > 0)
				{
					if (doFill)
					{
						progress += add;
						sync();
					}

					return (int) add;
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
			if (player == null)
			{
				return stack;
			}

			IItemHandler inv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

			if (inv == null)
			{
				return stack;
			}

			ItemStack stack1 = stack.copy();
			FluidActionResult result = FluidUtil.tryFillContainerAndStow(stack1, this, inv, Integer.MAX_VALUE, player, !simulate);

			if (!result.isSuccess())
			{
				result = FluidUtil.tryEmptyContainerAndStow(stack1, this, inv, Integer.MAX_VALUE, player, !simulate);
			}

			return result.isSuccess() ? result.getResult() : stack;
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return 1;
		}
	}
}