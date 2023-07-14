package dev.ftb.mods.ftbquests.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

public class RenderUtil {
    public static final int FULL_BRIGHT = 0x00F000F0;

    private final PoseStack matrixStack;
    private final VertexConsumer builder;
    private final float x;
    private final float y;

    private int packedLightIn = FULL_BRIGHT;
    private float u1 = 0f;
    private float v1 = 0f;
    private float u2 = 1f;
    private float v2 = 1f;
    private float w = 16f;
    private float h = 16f;
    private int color = 0xFFFFFFFF;

    private RenderUtil(PoseStack matrixStack, VertexConsumer builder, float x, float y) {
        this.matrixStack = matrixStack;
        this.builder = builder;
        this.x = x;
        this.y = y;
    }

    public static RenderUtil create(PoseStack matrixStack, VertexConsumer builder, float x, float y) {
        return new RenderUtil(matrixStack, builder, x, y);
    }

    public RenderUtil withUV(float u1, float v1, float u2, float v2) {
        this.u1 = u1;
        this.v1 = v1;
        this.u2 = u2;
        this.v2 = v2;
        return this;
    }

    public RenderUtil withSize(float w, float h) {
        this.w = w;
        this.h = h;
        return this;
    }

    public RenderUtil withColor(int color) {
        this.color = color;
        return this;
    }

    public RenderUtil withLighting(int packedLightIn) {
        this.packedLightIn = packedLightIn;
        return this;
    }

    public void draw() {
//        float r = ((color & 0xFF000000) >> 24) / 255F;
//        float g = ((color & 0x00FF0000) >> 16) / 255F;
//        float b = ((color & 0x0000FF00) >> 8) / 255F;
//        float a =  (color & 0x000000FF) / 255F;
        
        Matrix4f posMat = matrixStack.last().pose();
        builder.vertex(posMat, x, y + h, 0)
//                .color(r, g, b, a)
                .color(color)
                .uv(u1, v2)
                .uv2(packedLightIn)
                .endVertex();
        builder.vertex(posMat, x + w, y + h, 0)
//                .color(r, g, b, a)
                .color(color)
                .uv(u2, v2)
                .uv2(packedLightIn)
                .endVertex();
        builder.vertex(posMat, x + w, y, 0)
//                .color(r, g, b, a)
                .color(color)
                .uv(u2, v1)
                .uv2(packedLightIn)
                .endVertex();
        builder.vertex(posMat, x, y, 0)
//                .color(r, g, b, a)
                .color(color)
                .uv(u1, v1)
                .uv2(packedLightIn)
                .endVertex();
    }
}
