package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftbquests.block.BlockScreen;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import com.feed_the_beast.ftbquests.tile.TileScreenBase;
import com.feed_the_beast.ftbquests.tile.TileScreenCore;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

/**
 * @author LatvianModder
 */
public class RenderScreen extends TileEntitySpecialRenderer<TileScreenCore>
{
	@Override
	public void render(TileScreenCore screen, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
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

		RayTraceResult ray = ClientUtils.MC.objectMouseOver;

		if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK && ray.sideHit == screen.getFacing())
		{
			TileEntity tileEntity = screen.getWorld().getTileEntity(ray.getBlockPos());

			if (tileEntity == screen || tileEntity instanceof TileScreenBase)
			{
				TileScreenBase base = (TileScreenBase) tileEntity;

				if (base.getScreen() == screen)
				{
					mx = BlockScreen.getClickX(screen.facing, base.getOffsetX(), base.getOffsetZ(), ray.hitVec.x - ray.getBlockPos().getX(), ray.hitVec.z - ray.getBlockPos().getZ(), screen.size);
					my = BlockScreen.getClickY(base.getOffsetY(), ray.hitVec.y % 1D, screen.size);
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

		String top1 = task.quest.getDisplayName().getUnformattedText();
		String top2 = task.getDisplayName().getUnformattedText();

		if (top1.equals(top2) || top1.isEmpty())
		{
			top1 = top2;
			top2 = "";
		}

		if (my >= 0D && my <= 0.17D && !screen.indestructible.getBoolean() && task.quest.tasks.size() > 1)
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

		if (data == null)
		{
			bottomText = "???";
		}
		else
		{
			switch (screen.progressDisplayMode.getValue())
			{
				case PROGRESS:
					bottomText = data.getProgressString() + " / " + data.task.getMaxProgressString();
					break;
				case PERCENT:
					bottomText = (int) (data.getRelativeProgress() * 100D) + "%";
					break;
				default:
					char[] c = new char[12];
					Arrays.fill(c, ' ');
					c[0] = '[';
					c[11] = ']';
					int m = (int) (data.getRelativeProgress() * 10D);

					for (int i = 0; i < m; i++)
					{
						c[i + 1] = '#';
					}

					bottomText = new String(c);
			}
		}

		if (my >= 0.81D)
		{
			drawString(font, TextFormatting.GOLD + bottomText, 0.83D, 0.15D);
		}
		else if (data != null && data.getProgress() >= data.task.getMaxProgress())
		{
			drawString(font, TextFormatting.GREEN + bottomText, 0.83D, 0.15D);
		}
		else
		{
			drawString(font, bottomText, 0.83D, 0.15D);
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.5D, iconY, 0D);
		GlStateManager.scale(0.45D, 0.45D, 1D);
		ClientUtils.MC.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		ClientUtils.MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		GlStateManager.enableRescaleNormal();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1F, 1F, 1F, 1F);
		task.renderOnScreen(data);
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		ClientUtils.MC.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		ClientUtils.MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
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