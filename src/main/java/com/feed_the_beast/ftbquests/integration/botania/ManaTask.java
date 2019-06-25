package com.feed_the_beast.ftbquests.integration.botania;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.task.EnergyTask;
import com.feed_the_beast.ftbquests.quest.task.FTBQuestsTasks;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskData;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import com.feed_the_beast.ftbquests.quest.task.SimpleQuestTaskData;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenCore;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.mana.IManaReceiver;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ManaTask extends EnergyTask
{
	public static final ResourceLocation EMPTY_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/botania_mana_empty.png");
	public static final ResourceLocation FULL_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/botania_mana_full.png");

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
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);
		if (showNumbers)
		{
			nbt.setBoolean("show_numbers", true);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		showNumbers = nbt.getBoolean("show_numbers");
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeBoolean(showNumbers);
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		showNumbers = data.readBoolean();
	}

	@Override
	public String getAltTitle()
	{
		return showNumbers ? I18n.format("ftbquests.task.ftbquests.botania_mana.text", StringUtils.formatDouble(value, true)) : I18n.format("ftbquests.task.ftbquests.botania_mana.value");
	}

	@Override
	public Class<? extends TileTaskScreenCore> getScreenCoreClass()
	{
		return TileTaskScreenCoreBotania.class;
	}

	@Override
	public Class<? extends TileTaskScreenPart> getScreenPartClass()
	{
		return TileTaskScreenPartBotania.class;
	}

	@Override
	public TileTaskScreenCore createScreenCore(World world)
	{
		return new TileTaskScreenCoreBotania();
	}

	@Override
	public TileTaskScreenPart createScreenPart(World world)
	{
		return new TileTaskScreenPartBotania();
	}

	@Override
	public void getConfig(EntityPlayer player, ConfigGroup config)
	{
		super.getConfig(player, config);
		config.addBool("show_numbers", () -> showNumbers, v -> showNumbers = v, false);
	}

	@Override
	public void drawScreen(@Nullable QuestTaskData data)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		Minecraft mc = Minecraft.getMinecraft();

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

		double r = data == null ? 0D : data.getProgress() / (double) data.task.getMaxProgress();

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
	public boolean hideProgressNumbers()
	{
		return !showNumbers;
	}

	@Override
	public QuestTaskData createData(QuestData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<ManaTask> implements IManaReceiver
	{
		private Data(ManaTask task, QuestData data)
		{
			super(task, data);
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
				long add = Math.min(mana, task.value - progress);

				if (task.maxInput > 0)
				{
					add = Math.min(add, task.maxInput);
				}

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