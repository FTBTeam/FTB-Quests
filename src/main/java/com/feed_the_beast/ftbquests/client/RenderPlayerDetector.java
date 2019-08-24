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
		int w = 223;
		int a = 180;

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.disableFog();
		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		setLightmapDisabled(true);
		GlStateManager.glLineWidth(2.0F);
		bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.pos(minX, minY, minZ).color(w, w, w, a).endVertex();
		bufferbuilder.pos(minX, minY, minZ).color(w, w, w, a).endVertex();
		bufferbuilder.pos(maxX, minY, minZ).color(w, w, w, a).endVertex();
		bufferbuilder.pos(maxX, minY, maxZ).color(w, w, w, a).endVertex();
		bufferbuilder.pos(minX, minY, maxZ).color(w, w, w, a).endVertex();
		bufferbuilder.pos(minX, minY, minZ).color(w, w, w, a).endVertex();
		bufferbuilder.pos(minX, maxY, minZ).color(w, w, w, a).endVertex();
		bufferbuilder.pos(maxX, maxY, minZ).color(w, w, w, a).endVertex();
		bufferbuilder.pos(maxX, maxY, maxZ).color(w, w, w, a).endVertex();
		bufferbuilder.pos(minX, maxY, maxZ).color(w, w, w, a).endVertex();
		bufferbuilder.pos(minX, maxY, minZ).color(w, w, w, a).endVertex();
		bufferbuilder.pos(minX, maxY, maxZ).color(w, w, w, a).endVertex();
		bufferbuilder.pos(minX, minY, maxZ).color(w, w, w, a).endVertex();
		bufferbuilder.pos(maxX, minY, maxZ).color(w, w, w, a).endVertex();
		bufferbuilder.pos(maxX, maxY, maxZ).color(w, w, w, a).endVertex();
		bufferbuilder.pos(maxX, maxY, minZ).color(w, w, w, a).endVertex();
		bufferbuilder.pos(maxX, minY, minZ).color(w, w, w, a).endVertex();
		bufferbuilder.pos(maxX, minY, minZ).color(w, w, w, a).endVertex();
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