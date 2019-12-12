package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ForgeEnergyTask extends EnergyTask
{
	public static final ResourceLocation EMPTY_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/fe_empty.png");
	public static final ResourceLocation FULL_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/fe_full.png");

	public ForgeEnergyTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.FORGE_ENERGY;
	}

	@Override
	public String getAltTitle()
	{
		return I18n.format("ftbquests.task.ftbquests.forge_energy.text", StringUtils.formatDouble(value, true));
	}

	@Override
	public void drawScreen(@Nullable TaskData data)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		Minecraft mc = Minecraft.getInstance();

		mc.getTextureManager().bindTexture(EMPTY_TEXTURE);
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
			x -= 1D / 128D;
			w += 1D / 64D;

			h = r * 30D / 32D;
			y = 1D / 32D + (1D - r) * 30D / 32D - 0.5;

			y -= 1D / 128D;
			h += 1D / 64D;
			z = -0.003D;

			double u0 = 0;
			double v0 = 1D / 32D + (30D / 32D) * (1D - r);
			double u1 = 1;
			double v1 = 31D / 32D;

			mc.getTextureManager().bindTexture(FULL_TEXTURE);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(x, y + h, z).tex(u0, v1).endVertex();
			buffer.pos(x + w, y + h, z).tex(u1, v1).endVertex();
			buffer.pos(x + w, y, z).tex(u1, v0).endVertex();
			buffer.pos(x, y, z).tex(u0, v0).endVertex();
			tessellator.draw();
		}
	}

	@Override
	public TaskData createData(PlayerData data)
	{
		return new Data(this, data);
	}

	public static class Data extends TaskData<ForgeEnergyTask> implements IEnergyStorage
	{
		private final LazyOptional<IEnergyStorage> energyStorageProvider;

		private Data(ForgeEnergyTask task, PlayerData data)
		{
			super(task, data);
			energyStorageProvider = LazyOptional.of(() -> this);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing)
		{
			return capability == CapabilityEnergy.ENERGY ? energyStorageProvider.cast() : LazyOptional.empty();
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate)
		{
			if (maxReceive > 0 && !isComplete())
			{
				long add = Math.min(maxReceive, task.value - progress);

				if (task.maxInput > 0)
				{
					add = Math.min(add, task.maxInput);
				}

				if (add > 0L)
				{
					if (!simulate)
					{
						addProgress(add);
					}

					return (int) add;
				}
			}

			return 0;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate)
		{
			return 0;
		}

		@Override
		public int getEnergyStored()
		{
			return 0;
		}

		@Override
		public int getMaxEnergyStored()
		{
			return (int) Math.min(task.maxInput, Integer.MAX_VALUE);
		}

		@Override
		public boolean canExtract()
		{
			return false;
		}

		@Override
		public boolean canReceive()
		{
			return true;
		}
	}
}