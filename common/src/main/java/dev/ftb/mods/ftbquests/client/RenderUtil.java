package dev.ftb.mods.ftbquests.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Matrix4f;

public class RenderUtil {
    public static final int FULL_BRIGHT = 0x00F000F0;

    private final PoseStack.Pose pose;
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
    private float zOffset = 0f;
    private int color = 0xFFFFFFFF;

    private RenderUtil(PoseStack.Pose pose, VertexConsumer builder, float x, float y) {
        this.pose = pose;
        this.builder = builder;
        this.x = x;
        this.y = y;
    }

    public static RenderUtil create(PoseStack.Pose pose, VertexConsumer builder, float x, float y) {
        return new RenderUtil(pose, builder, x, y);
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

    public RenderUtil withZOffset(float zOffset) {
        this.zOffset = zOffset;
        return this;
    }

    public RenderUtil withLighting(int packedLightIn) {
        this.packedLightIn = packedLightIn;
        return this;
    }

    public void draw() {
        Matrix4f posMat = pose.pose();
        builder.addVertex(posMat, x, y + h, zOffset)
                .setColor(color)
                .setUv(u1, v2)
                .setLight(packedLightIn);
        builder.addVertex(posMat, x + w, y + h, zOffset)
                .setColor(color)
                .setUv(u2, v2)
                .setLight(packedLightIn);
        builder.addVertex(posMat, x + w, y, zOffset)
                .setColor(color)
                .setUv(u2, v1)
                .setLight(packedLightIn);
        builder.addVertex(posMat, x, y, zOffset)
                .setColor(color)
                .setUv(u1, v1)
                .setLight(packedLightIn);
    }
}
