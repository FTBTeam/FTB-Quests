package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftbquests.block.BlockScreen;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenBase;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenCore;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public class RenderProgressScreen extends TileEntitySpecialRenderer<TileProgressScreenCore>
{
	@Override
	public void render(TileProgressScreenCore screen, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if (!ClientQuestFile.exists())
		{
			return;
		}
		else if (screen.width == 0 && screen.height == 0)
		{
			BlockPos pos = screen.getPos().offset(screen.getFacing());
			IBlockState state = screen.getWorld().getBlockState(pos);

			if (!state.getBlock().isReplaceable(screen.getWorld(), pos))
			{
				return;
			}
		}

		QuestChapter chapter = screen.getChapter();

		if (chapter == null)
		{
			return;
		}

		double mx = -1D, my = -1D;

		RayTraceResult ray = ClientUtils.MC.objectMouseOver;

		if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK && ray.sideHit == screen.getFacing())
		{
			TileEntity tileEntity = screen.getWorld().getTileEntity(ray.getBlockPos());

			if (tileEntity == screen || tileEntity instanceof TileProgressScreenBase)
			{
				TileProgressScreenBase base = (TileProgressScreenBase) tileEntity;

				if (base.getScreen() == screen)
				{
					mx = BlockScreen.getClickX(screen.facing, base.getOffsetX(), base.getOffsetZ(), ray.hitVec.x - ray.getBlockPos().getX(), ray.hitVec.z - ray.getBlockPos().getZ(), screen.width);
					my = BlockScreen.getClickY(base.getOffsetY(), ray.hitVec.y % 1D, screen.height);
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

		GlStateManager.translate(-screen.width, -screen.height - 1, -0.01F);
		GlStateManager.scale(screen.width * 2 + 1, screen.height + 2, 1);

		String top1 = chapter.getDisplayName().getUnformattedText();

		drawString(font, top1, 0.02, 0.15);

		drawString(font, TextFormatting.DARK_PURPLE + "WIP", 0.35, 0.3);

		IProgressData team = screen.getTeam();

		String bottomText = team == null ? "???" : team.getTeamID();
		drawString(font, TextFormatting.DARK_GREEN + bottomText, 0.83, 0.15);

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