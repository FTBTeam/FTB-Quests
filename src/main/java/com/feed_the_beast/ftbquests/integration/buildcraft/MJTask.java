package com.feed_the_beast.ftbquests.integration.buildcraft;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.task.EnergyTask;
import com.feed_the_beast.ftbquests.quest.task.FTBQuestsTasks;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskData;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import com.feed_the_beast.ftbquests.quest.task.SimpleQuestTaskData;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class MJTask extends EnergyTask
{
	private static final ResourceLocation EMPTY_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/fe_empty.png");
	private static final ResourceLocation FULL_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/fe_full.png");

	public MJTask(Quest quest)
	{
		super(quest);
		value = 10000000000L;
	}

	@Override
	public QuestTaskType getType()
	{
		return FTBQuestsTasks.BUILDCRAFT_MJ;
	}

	@Override
	public String getMaxProgressString()
	{
		return StringUtils.formatDouble(value / 1000000D, true);
	}

	@Override
	public Icon getAltIcon()
	{
		return Icon.getIcon(EMPTY_TEXTURE.toString()).combineWith(Icon.getIcon(FULL_TEXTURE.toString()));
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return new TextComponentTranslation("ftbquests.task.ftbquests.buildcraft_mj.text", StringUtils.formatDouble(value / 1000000D, true));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawGUI(@Nullable QuestTaskData data, int x, int y, int w, int h)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		ClientUtils.MC.getTextureManager().bindTexture(EMPTY_TEXTURE);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x, y + h, 0).tex(0, 1).endVertex();
		buffer.pos(x + w, y + h, 0).tex(1, 1).endVertex();
		buffer.pos(x + w, y, 0).tex(1, 0).endVertex();
		buffer.pos(x, y, 0).tex(0, 0).endVertex();
		tessellator.draw();

		double r = data == null ? 0D : data.getRelativeProgress() / 100D;

		if (r > 0D)
		{
			double h1 = (r * 30D / 32D) * h;
			double y1 = y + (1D / 32D + (1D - r) * 30D / 32D) * h;

			double v0 = 1D / 32D + (30D / 32D) * (1D - r);
			double v1 = 31D / 32D;

			ClientUtils.MC.getTextureManager().bindTexture(FULL_TEXTURE);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(x, y1 + h1, 0).tex(0, v1).endVertex();
			buffer.pos(x + w, y1 + h1, 0).tex(1, v1).endVertex();
			buffer.pos(x + w, y1, 0).tex(1, v0).endVertex();
			buffer.pos(x, y1, 0).tex(0, v0).endVertex();
			tessellator.draw();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawScreen(@Nullable QuestTaskData data)
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

		double r = data == null ? 0D : data.getRelativeProgress() / 100D;

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
	public QuestTaskData createData(ITeamData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<MJTask> implements IMjReceiver
	{
		private Data(MJTask task, ITeamData data)
		{
			super(task, data);
		}

		@Override
		public String getProgressString()
		{
			return StringUtils.formatDouble(progress / 1000000D, true);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
		{
			return capability == MjAPI.CAP_RECEIVER || capability == MjAPI.CAP_CONNECTOR;
		}

		@Override
		@Nullable
		public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
		{
			return capability == MjAPI.CAP_RECEIVER || capability == MjAPI.CAP_CONNECTOR ? (T) this : null;
		}

		@Override
		public long getPowerRequested()
		{
			long add = task.value - progress;

			if (task.maxInput > 0)
			{
				add = Math.min(add, task.maxInput);
			}

			return add;
		}

		@Override
		public long receivePower(long microJoules, boolean simulate)
		{
			if (microJoules > 0L && progress < task.value)
			{
				long add = Math.min(microJoules, getPowerRequested());

				if (add > 0L)
				{
					if (!simulate)
					{
						progress += add;
						sync();
					}

					return microJoules - add;
				}
			}

			return microJoules;
		}

		@Override
		public boolean canConnect(@Nonnull IMjConnector other)
		{
			return true;
		}
	}
}