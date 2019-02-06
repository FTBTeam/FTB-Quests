package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.block.BlockTaskScreen;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskData;
import com.feed_the_beast.ftbquests.tile.ITaskScreen;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

/**
 * @author LatvianModder
 */
public class RenderTaskScreen extends TileEntitySpecialRenderer<TileTaskScreenCore>
{
	@Override
	public void render(TileTaskScreenCore screen, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if (!ClientQuestFile.exists())
		{
			return;
		}

		QuestTask task = screen.getTask();

		if (task == null)
		{
			return;
		}

		double mx = -1D, my = -1D;

		RayTraceResult ray = Minecraft.getMinecraft().objectMouseOver;

		if (!screen.inputOnly && ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK && ray.sideHit == screen.getFacing())
		{
			TileEntity tileEntity = screen.getWorld().getTileEntity(ray.getBlockPos());

			if (tileEntity == screen || tileEntity instanceof ITaskScreen)
			{
				ITaskScreen base = (ITaskScreen) tileEntity;

				if (base.getScreen() == screen)
				{
					mx = BlockTaskScreen.getClickX(screen.facing, base.getOffsetX(), base.getOffsetZ(), ray.hitVec.x - ray.getBlockPos().getX(), ray.hitVec.z - ray.getBlockPos().getZ(), screen.size);
					my = BlockTaskScreen.getClickY(base.getOffsetY(), ray.hitVec.y % 1D, screen.size);
				}
			}
		}

		GlStateManager.color(1F, 1F, 1F, alpha);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.glNormal3f(0F, 1F, 0F);
		GlStateManager.translate(0.5F, 0.5F, 0.5F);
		GlStateManager.rotate(180F, 0F, 0F, 1F);
		GlStateManager.rotate(screen.getFacing().getHorizontalAngle() + 180F, 0F, 1F, 0F);
		GlStateManager.translate(-0.5F, -0.5F, -0.5F);
		setLightmapDisabled(true);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.depthMask(true);
		GlStateManager.enableAlpha();
		FontRenderer font = getFontRenderer();
		boolean flag = font.getUnicodeFlag();
		font.setUnicodeFlag(true);

		GlStateManager.translate(-screen.size, -screen.size * 2F, -0.01F);
		GlStateManager.scale(screen.size * 2D + 1D, screen.size * 2D + 1D, 1D);

		String top1 = screen.inputOnly ? "" : task.quest.getDisplayName().getUnformattedText();
		String top2 = screen.inputOnly ? "" : task.getDisplayName().getUnformattedText();

		if (top1.isEmpty() || top1.equals(top2))
		{
			top1 = top2;
			top2 = "";
		}

		if (my >= 0D && my <= 0.17D && !screen.indestructible && task.quest.tasks.size() > 1)
		{
			top1 = TextFormatting.GOLD + top1;
		}

		drawString(font, top1, 0.02D, 0.15D);
		double iconY = 0.5D;

		if (!top2.isEmpty())
		{
			drawString(font, top2, 0.17D, 0.07D);
			iconY = 0.54D;
		}

		QuestTaskData data = screen.getTaskData();

		String bottomText;

		if (screen.inputOnly)
		{
			bottomText = "";
		}
		else if (data == null)
		{
			bottomText = "???";
		}
		else
		{
			bottomText = data.task.hideProgressNumbers() ? "" : (data.getProgressString() + " / " + data.task.getMaxProgressString());
		}

		if (data != null && !bottomText.isEmpty() && data.getProgress() >= data.task.getMaxProgress())
		{
			drawString(font, TextFormatting.GREEN + bottomText, 0.83D, 0.15D);
		}
		else
		{
			drawString(font, bottomText, 0.83D, 0.15D);
		}

		GlStateManager.color(1F, 1F, 1F, 1F);

		if (screen.inputOnly)
		{
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			TextureAtlasSprite sprite = FTBQuestsClientEventHandler.inputBlockSprite;
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(0, 1, 0).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
			buffer.pos(1, 1, 0).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
			buffer.pos(1, 0, 0).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
			buffer.pos(0, 0, 0).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
			tessellator.draw();
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.5D, iconY, 0D);
		GlStateManager.scale(screen.inputOnly ? 0.5 : 0.45, screen.inputOnly ? 0.5 : 0.45, 1);
		GlStateManager.enableRescaleNormal();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1F, 1F, 1F, 1F);

		if (screen.inputOnly && !screen.inputModeIcon.isEmpty())
		{
			ItemIcon.drawItem3D(screen.inputModeIcon);
		}
		else
		{
			task.drawScreen(data);
		}

		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();

		font.setUnicodeFlag(flag);
		setLightmapDisabled(false);
		GlStateManager.enableLighting();
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.popMatrix();
	}

	private void drawString(FontRenderer font, String string, double y, double size)
	{
		if (string.isEmpty())
		{
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.5D, y, 0D);
		int len = font.getStringWidth(string);
		double scale = size / 9D;
		double w = len * scale;

		if (w > 1D)
		{
			scale /= w;
			w = 1D;
		}

		if (w > 0.9D)
		{
			scale *= 0.9D;
		}

		GlStateManager.scale(scale, scale, 1D);
		font.drawString(string, -len / 2, 0, 0xFFD8D8D8);
		GlStateManager.popMatrix();
	}
}