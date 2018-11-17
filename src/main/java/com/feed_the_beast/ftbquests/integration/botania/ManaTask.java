package com.feed_the_beast.ftbquests.integration.botania;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.task.FTBQuestsTasks;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskData;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import com.feed_the_beast.ftbquests.quest.task.SimpleQuestTaskData;
import com.feed_the_beast.ftbquests.tile.TileScreenCore;
import com.feed_the_beast.ftbquests.tile.TileScreenPart;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.mana.IManaReceiver;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ManaTask extends QuestTask
{
	private static final ResourceLocation EMPTY_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/botania_mana_empty.png");
	private static final ResourceLocation FULL_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/botania_mana_full.png");

	public long value = 1, maxInput = Long.MAX_VALUE;
	public boolean showNumbers;

	public ManaTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public QuestTaskType getType()
	{
		return FTBQuestsTasks.BOTANIA_MANA;
	}

	@Override
	public long getMaxProgress()
	{
		return value;
	}

	@Override
	public String getMaxProgressString()
	{
		return StringUtils.formatDouble(value, true);
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		nbt.setLong("value", value);

		if (maxInput != Long.MAX_VALUE)
		{
			nbt.setLong("max_input", maxInput);
		}

		if (showNumbers)
		{
			nbt.setBoolean("show_numbers", true);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		value = nbt.getLong("value");

		if (value < 1)
		{
			value = 1;
		}

		maxInput = nbt.hasKey("max_input") ? nbt.getLong("max_input") : Long.MAX_VALUE;

		if (maxInput < 1)
		{
			maxInput = 1;
		}

		showNumbers = nbt.getBoolean("show_numbers");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeVarLong(value);
		data.writeLong(maxInput);
		data.writeBoolean(showNumbers);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		value = data.readVarLong();
		maxInput = data.readLong();
		showNumbers = data.readBoolean();
	}

	@Override
	public Icon getAltIcon()
	{
		return Icon.getIcon(FULL_TEXTURE.toString()).combineWith(Icon.getIcon(EMPTY_TEXTURE.toString()));
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return showNumbers ? new TextComponentTranslation("ftbquests.task.ftbquests.botania_mana.text", StringUtils.formatDouble(value, true)) : new TextComponentTranslation("ftbquests.task.ftbquests.botania_mana.value");
	}

	@Override
	public Class<? extends TileScreenCore> getScreenCoreClass()
	{
		return TileScreenCoreBotania.class;
	}

	@Override
	public Class<? extends TileScreenPart> getScreenPartClass()
	{
		return TileScreenPartBotania.class;
	}

	@Override
	public TileScreenCore createScreenCore(World world)
	{
		return new TileScreenCoreBotania();
	}

	@Override
	public TileScreenPart createScreenPart(World world)
	{
		return new TileScreenPartBotania();
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		config.addLong("value", () -> value, v -> value = v, 1, 1, Long.MAX_VALUE);
		config.addLong("max_input", () -> maxInput, v -> maxInput = v, Long.MAX_VALUE, 1, Long.MAX_VALUE);
		config.addBool("show_numbers", () -> showNumbers, v -> showNumbers = v, false);
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
			double y1 = y + (3D / 32D + (1D - r) * 26D / 32D) * h;
			double h1 = (r * 26D / 32D) * h;

			double v0 = 3D / 32D + (26D / 32D) * (1D - r);
			double v1 = 29D / 32D;

			ClientUtils.MC.getTextureManager().bindTexture(FULL_TEXTURE);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(x, y1 + h1, 0).tex(0, v1).endVertex();
			buffer.pos(x + w, y1 + h1, 0).tex(1, v1).endVertex();
			buffer.pos(x + w, y1, 0).tex(1, v0).endVertex();
			buffer.pos(x, y1, 0).tex(0, v0).endVertex();
			tessellator.draw();
		}

		ClientUtils.MC.getTextureManager().bindTexture(EMPTY_TEXTURE);
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

			y = 3D / 32D + (1D - r) * 26D / 32D - 0.5;
			h = r * 26D / 32D;

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
	public boolean hideProgressNumbers()
	{
		return !showNumbers;
	}

	@Override
	public QuestTaskData createData(ITeamData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<ManaTask> implements IManaReceiver
	{
		private Data(ManaTask task, ITeamData data)
		{
			super(task, data);
		}

		@Override
		public String getProgressString()
		{
			return StringUtils.formatDouble(progress, true);
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

		@Override
		public boolean isFull()
		{
			return progress >= task.value;
		}

		@Override
		public void recieveMana(int mana)
		{
			if (mana > 0 && progress < task.value)
			{
				long add = Math.min(task.maxInput, Math.min(mana, task.value - progress));

				if (add > 0L)
				{
					progress += add;
					sync();
				}
			}
		}

		@Override
		public boolean canRecieveManaFromBursts()
		{
			return !isFull();
		}

		@Override
		public int getCurrentMana()
		{
			return 0;
		}
	}
}