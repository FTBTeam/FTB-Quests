package com.feed_the_beast.ftbquests.integration.ic2;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigDouble;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import com.feed_the_beast.ftbquests.tile.TileScreenCore;
import com.feed_the_beast.ftbquests.tile.TileScreenPart;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class IC2EnergyTask extends QuestTask
{
	public static final String ID = "ic2_energy";
	private static final ResourceLocation EMPTY_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/ic2_empty.png");
	private static final ResourceLocation FULL_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/ic2_full.png");

	public final ConfigDouble value;
	public final ConfigDouble maxInput;

	public IC2EnergyTask(Quest quest, NBTTagCompound nbt)
	{
		super(quest);
		value = new ConfigDouble(nbt.getDouble("value"), 1D, Double.POSITIVE_INFINITY);
		maxInput = new ConfigDouble(nbt.hasKey("max_input") ? nbt.getDouble("max_input") : Double.POSITIVE_INFINITY, 1D, Double.POSITIVE_INFINITY);
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
		nbt.setDouble("value", value.getDouble());
		nbt.setDouble("max_input", maxInput.getDouble());
	}

	@Override
	public Icon getIcon()
	{
		return Icon.getIcon("item:ic2:te 1 74");
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentTranslation("ftbquests.task.ic2_energy.text", StringUtils.formatDouble(value.getDouble(), true));
	}

	@Override
	public Class<? extends TileScreenCore> getScreenCoreClass()
	{
		return TileScreenCoreIC2.class;
	}

	@Override
	public Class<? extends TileScreenPart> getScreenPartClass()
	{
		return TileScreenPartIC2.class;
	}

	@Override
	public TileScreenCore createScreenCore(World world)
	{
		return new TileScreenCoreIC2();
	}

	@Override
	public TileScreenPart createScreenPart(World world)
	{
		return new TileScreenPartIC2();
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("value", value, new ConfigDouble(1));
		group.add("max_input", maxInput, new ConfigDouble(Double.POSITIVE_INFINITY));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderOnScreen(@Nullable QuestTaskData data)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		ClientUtils.MC.getTextureManager().bindTexture(EMPTY_TEXTURE);
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

		double r = data == null ? 0D : data.getRelativeProgress();

		if (r > 0D)
		{
			x -= 1D / 128D;
			w += 1D / 64D;

			h = r * 26D / 32D;
			y = 3D / 32D + (1D - r) * 26D / 32D - 0.5;

			y -= 1D / 128D;
			h += 1D / 64D;
			z = 0.003D;

			double u0 = 0;
			double v0 = 3D / 32D + (26D / 32D) * (1D - r);
			double u1 = 1;
			double v1 = 29D / 32D;

			ClientUtils.MC.getTextureManager().bindTexture(FULL_TEXTURE);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(x, y + h, z).tex(u0, v1).endVertex();
			buffer.pos(x + w, y + h, z).tex(u1, v1).endVertex();
			buffer.pos(x + w, y, z).tex(u1, v0).endVertex();
			buffer.pos(x, y, z).tex(u0, v0).endVertex();
			tessellator.draw();
		}
	}

	@Override
	public QuestTaskData createData(IProgressData data)
	{
		return new Data(this, data);
	}

	public static class Data extends QuestTaskData<IC2EnergyTask>
	{
		public double energy;

		private Data(IC2EnergyTask task, IProgressData data)
		{
			super(task, data);
		}

		@Override
		public NBTBase toNBT()
		{
			return energy > 0 ? new NBTTagDouble(energy) : null;
		}

		@Override
		public void fromNBT(@Nullable NBTBase nbt)
		{
			if (nbt instanceof NBTPrimitive)
			{
				energy = ((NBTPrimitive) nbt).getDouble();
			}
			else
			{
				energy = 0D;
			}
		}

		@Override
		public long getProgress()
		{
			return (long) energy;
		}

		@Override
		public double getRelativeProgress()
		{
			return energy / task.value.getDouble();
		}

		@Override
		public String getProgressString()
		{
			return StringUtils.formatDouble(energy, true);
		}

		@Override
		public void resetProgress()
		{
			energy = 0D;
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
			if (amount > 0 && energy < task.value.getDouble())
			{
				double add = Math.min(task.maxInput.getDouble(), Math.min(amount, task.value.getDouble() - energy));

				if (add > 0D)
				{
					energy += add;
					data.syncTask(this);
					return amount - add;
				}
			}

			return amount;
		}
	}
}