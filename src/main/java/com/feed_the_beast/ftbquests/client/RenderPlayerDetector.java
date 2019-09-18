package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftbquests.tile.TilePlayerDetector;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * @author LatvianModder
 */
public class RenderPlayerDetector extends TileEntitySpecialRenderer<TilePlayerDetector>
{
	@Override
	public void render(TilePlayerDetector detector, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if (!ClientQuestFile.exists() || !ClientQuestFile.INSTANCE.canEdit())
		{
			return;
		}

		BlockPos pos = detector.getPos();
		AxisAlignedBB aabb = detector.getAABB();
		double minX = x + aabb.minX - pos.getX();
		double minY = y + aabb.minY - pos.getY();
		double minZ = z + aabb.minZ - pos.getZ();
		double maxX = x + aabb.maxX - pos.getX();
		double maxY = y + aabb.maxY - pos.getY();
		double maxZ = z + aabb.maxZ - pos.getZ();
		int col = Color.getHSBColor((detector.hashCode() & 31) / 31F, 0.5F, 1F).getRGB();
		int r = (col >> 16) & 0xFF;
		int g = (col >> 8) & 0xFF;
		int b = col & 0xFF;
		int a = 33;

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.disableFog();
		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		setLightmapDisabled(true);
		GlStateManager.glLineWidth(2F);
		GlStateManager.disableDepth();

		bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
		tessellator.draw();
		GlStateManager.enableDepth();

		a = 180;
		bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
		bufferbuilder.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
		tessellator.draw();

		GlStateManager.glLineWidth(1.0F);
		setLightmapDisabled(false);
		GlStateManager.glLineWidth(1.0F);
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		GlStateManager.enableFog();
	}
}