package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.block.TileScreen;
import com.feed_the_beast.ftbquests.block.TileScreenBase;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
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
public class RenderScreen extends TileEntitySpecialRenderer<TileScreen>
{
	@Override
	public void render(TileScreen screen, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if (!ClientQuestFile.existsWithTeam())
		{
			return;
		}

		QuestTaskData data = screen.getTaskData();

		if (data == null)
		{
			return;
		}

		double mx = -1D, my = -1D;

		RayTraceResult ray = ClientUtils.MC.objectMouseOver;

		if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK && ray.sideHit == screen.getFacing())
		{
			TileEntity tileEntity = screen.getWorld().getTileEntity(ray.getBlockPos());

			if (tileEntity == screen || tileEntity instanceof TileScreenBase && ((TileScreenBase) tileEntity).getScreen() == screen)
			{
				mx = 0.5D; //FIXME: X coordinate
				my = 1D - (((TileScreenBase) tileEntity).getOffsetY() + ray.hitVec.y % 1D) / (screen.size * 2D + 1D);
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

		drawString(font, data.task.quest.getDisplayName().getFormattedText(), 0.02D, 0.15D);
		drawString(font, data.task.getDisplayName().getFormattedText(), 0.17D, 0.07D);

		String bottomText;

		if (screen.amountMode == 0)
		{
			bottomText = data.getProgress() + " / " + data.task.getMaxProgress();
		}
		else if (screen.amountMode == 1)
		{
			bottomText = (data.getProgress() * 100 / data.task.getMaxProgress()) + "%";
		}
		else
		{
			char[] c = new char[12];
			Arrays.fill(c, ' ');
			c[0] = '[';
			c[11] = ']';
			int m = data.getProgress() * 10 / data.task.getMaxProgress();

			for (int i = 0; i < m; i++)
			{
				c[i + 1] = '#';
			}

			bottomText = new String(c);
		}

		if (my >= 0.81D)
		{
			drawString(font, TextFormatting.GOLD + bottomText, 0.83D, 0.15D);
		}
		else
		{
			drawString(font, bottomText, 0.83D, 0.15D);
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.5D, 0.54D, 0D);
		GlStateManager.scale(0.5D, 0.5D, 1D);
		ClientUtils.MC.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		ClientUtils.MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		GlStateManager.enableRescaleNormal();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1F, 1F, 1F, 1F);
		screen.getIcon().draw3D(screen.getWorld(), Icon.EMPTY);
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
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.5D, y, 0D);
		GlStateManager.scale(size / 9D, size / 9D, 1D);
		font.drawString(string, -font.getStringWidth(string) / 2, 0, 0xFFD8D8D8);
		GlStateManager.popMatrix();
	}
}