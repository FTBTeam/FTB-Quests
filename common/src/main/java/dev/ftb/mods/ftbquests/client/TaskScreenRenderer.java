package dev.ftb.mods.ftbquests.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.architectury.hooks.fluid.FluidStackHooks;
import dev.ftb.mods.ftblibrary.icon.AtlasSpriteIcon;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.block.TaskScreenBlock;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.EnergyTask;
import dev.ftb.mods.ftbquests.quest.task.FluidTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class TaskScreenRenderer implements BlockEntityRenderer<TaskScreenBlockEntity> {
    public static final ResourceLocation INPUT_ONLY_TEXTURE = FTBQuestsAPI.rl("tasks/input_only");
    public static final ResourceLocation TANK_TEXTURE = FTBQuestsAPI.rl("tasks/tank");
    public static final ResourceLocation FE_ENERGY_EMPTY_TEXTURE = FTBQuestsAPI.rl("tasks/fe_empty");
    public static final ResourceLocation FE_ENERGY_FULL_TEXTURE = FTBQuestsAPI.rl("tasks/fe_full");
    public static final ResourceLocation TR_ENERGY_EMPTY_TEXTURE = FTBQuestsAPI.rl("tasks/ic2_empty");
    public static final ResourceLocation TR_ENERGY_FULL_TEXTURE = FTBQuestsAPI.rl("tasks/ic2_full");

    private final BlockEntityRendererProvider.Context context;

    public TaskScreenRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public boolean shouldRender(TaskScreenBlockEntity blockEntity, Vec3 vec3) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, vec3);
    }

    @Override
    public void render(TaskScreenBlockEntity taskScreen, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLight, int combinedOverlay) {
        if (!ClientQuestFile.exists() || !(taskScreen.getBlockState().getBlock() instanceof TaskScreenBlock taskScreenBlock)) return;

        TeamData data = ClientQuestFile.INSTANCE.getNullableTeamData(taskScreen.getTeamId());
        Task task = taskScreen.getTask();
        if (task == null || data == null) return;

        poseStack.pushPose();

        poseStack.translate(0.5D, 0.5D, 0.5D);
        float rotation = taskScreen.getBlockState().getValue(WallSignBlock.FACING).toYRot() + 180f;
        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        int size = taskScreenBlock.getSize() / 2;
        poseStack.translate(-size, -size * 2F, -0.02F);
        poseStack.scale(size * 2F + 1F, size * 2F + 1F, 1F);

        Font font = context.getFont();
        double iconY = 0.5D;

        // render quest and task title at top of screen
        Component top1 = taskScreen.isInputOnly() ? Component.empty() : task.getQuest().getTitle();
        Component top2 = taskScreen.isInputOnly() ? Component.empty() : task.getTitle();
        drawString(taskScreen, font, multiBufferSource, poseStack, top1, 0.02D, 0.15F);
        if (!top2.equals(Component.empty())) {
            drawString(taskScreen, font, multiBufferSource, poseStack, top2, 0.17D, 0.07F);
            iconY = 0.54D;
        }

        // render progress numbers at bottom of screen
        if (!taskScreen.isInputOnly() && !task.hideProgressNumbers() /*&& data.isCompleted(task)*/) {
            long progress = data.getProgress(task);
            ChatFormatting col = progress == 0 ? ChatFormatting.GOLD : (progress < task.getMaxProgress() ? ChatFormatting.YELLOW : ChatFormatting.GREEN);
            Component txt = Component.literal(task.formatProgress(data, progress) + " / " + task.formatMaxProgress()).withStyle(col);
            drawString(taskScreen, font, multiBufferSource, poseStack, txt, 0.83D, 0.15F);
        }

        // render icon for task item/fluid/energy
        poseStack.pushPose();
        poseStack.translate(0.5D, iconY, -0.01D);
        poseStack.scale(taskScreen.isInputOnly() ? 0.5f : 0.45f, taskScreen.isInputOnly() ? 0.5f : 0.45f, 0.2f * size);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
        if (taskScreen.isInputOnly() && !taskScreen.getInputModeIcon().isEmpty()) {
            drawTaskIcon(taskScreen, data, ItemIcon.getItemIcon(taskScreen.getInputModeIcon()), poseStack, multiBufferSource);
        } else {
            drawTaskIcon(taskScreen, data, task.getIcon(), poseStack, multiBufferSource);
        }
        poseStack.popPose();

        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.text(InventoryMenu.BLOCK_ATLAS));

        // render skin, if needed
        float[] f = taskScreen.getFakeTextureUV();
        if (f != null && f.length == 4) {
            poseStack.pushPose();
            poseStack.scale(1 / 16f, 1 / 16f, 1 / 16f);
            poseStack.translate(0, 0, 0.01f);
            RenderUtil.create(poseStack, vertexConsumer, 0f, 0f).withUV(f[0], f[1], f[2], f[3]).draw();
            poseStack.popPose();
        }

        // render input-only texture, if needed
        if (taskScreen.isInputOnly() && FTBQuestsClientEventHandler.inputOnlySprite != null) {
            TextureAtlasSprite s = FTBQuestsClientEventHandler.inputOnlySprite;
            poseStack.pushPose();
            poseStack.scale(1 / 16f, 1 / 16f, 1 / 16f);
            RenderUtil.create(poseStack, vertexConsumer, 0, 0).withUV(s.getU0(), s.getV0(), s.getU1(), s.getV1()).draw();
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    // FIXME: FTB Library should handle this, but its 3d icon rendering needs rewriting (it doesn't properly use MultiBufferSource)
    private void drawTaskIcon(TaskScreenBlockEntity taskScreen, TeamData data, Icon icon, PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.text(InventoryMenu.BLOCK_ATLAS));

        Task task = taskScreen.getTask();
        long progress = data.getProgress(task);

        poseStack.pushPose();
        poseStack.scale(1 / 16f, 1 / 16f, 1 / 16f);

        if (icon instanceof IconAnimation anim) {
            icon = anim.list.get((int)(System.currentTimeMillis() / 1000L % (long)anim.list.size()));
        }

        if (task instanceof FluidTask fluidTask && fluidTask.getIcon() instanceof AtlasSpriteIcon as && FTBQuestsClientEventHandler.tankSprite != null) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(as.getId());
            // fluid texture (interpolated according to task progress)
            if (progress > 0L) {
                float interpolatedProgress = (float) ((double) progress / task.getMaxProgress());
                RenderUtil.create(poseStack, vertexConsumer, -8f, -8f)
                        .withColor(FluidStackHooks.getColor(fluidTask.getFluid()) | 0xFF000000)
                        .withSize(16f, interpolatedProgress * 16f)
                        .withUV(sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV(interpolatedProgress))
                        .draw();
            }
            // tank overlay
            TextureAtlasSprite s = FTBQuestsClientEventHandler.tankSprite;
            poseStack.translate(0, 0, -0.05f);
            RenderUtil.create(poseStack, vertexConsumer, -8f, -8f)
                    .withUV(s.getU0(), s.getV0(), s.getU1(), s.getV1())
                    .draw();
        } else if (task instanceof EnergyTask energyTask) {
            TextureAtlasSprite empty = energyTask.getClientData().getEmptyTexture();
            TextureAtlasSprite full = energyTask.getClientData().getFullTexture();
            RenderUtil.create(poseStack, vertexConsumer, -8f, -8f)
                    .withUV(empty.getU0(), empty.getV0(), empty.getU1(), empty.getV1())
                    .draw();
            if (progress > 0L) {
                float interpolatedProgress = (float) ((double) progress / task.getMaxProgress());
                poseStack.translate(0, 0, -0.05f);
                RenderUtil.create(poseStack, vertexConsumer, -8f, -8f)
                        .withSize(16f, interpolatedProgress * 16f)
                        .withUV(full.getU0(), full.getV0(), full.getU1(), full.getV(interpolatedProgress))
                        .draw();
            }
        } else if (icon instanceof ItemIcon itemIcon) {
            // TODO a flat 3d-render (GUI style) would be better here
            poseStack.scale(16f, 16f, 16f);
            Minecraft.getInstance().getItemRenderer().renderStatic(itemIcon.getStack(), ItemDisplayContext.FIXED, RenderUtil.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY, poseStack, buffer, Minecraft.getInstance().level, 0);
        } else if (icon instanceof AtlasSpriteIcon spriteIcon) {
            var sprite = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(spriteIcon.getId());
            RenderUtil.create(poseStack, vertexConsumer, -8f, -8f)
                    .withUV(sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1())
                    .draw();
        }

        poseStack.popPose();
    }

    private void drawString(TaskScreenBlockEntity taskScreen, Font font, MultiBufferSource bufferSource, PoseStack poseStack, Component text, double y, float size) {
        if (!text.equals(Component.empty())) {
            poseStack.pushPose();
            poseStack.translate(0.5D, y, 0D);

            int len = font.width(text);
            float scale = size / 9F;
            float width = len * scale;
            if (width > 1F) {
                scale /= width;
                width = 1F;
            }
            if (width > 0.9F) {
                scale *= 0.9F;
            }

            poseStack.scale(scale, scale, 1F);
            Matrix4f posMat = poseStack.last().pose();
            font.drawInBatch(text, -len / 2f, 0, 0xFFD8D8D8, taskScreen.isTextShadow(), posMat, bufferSource, Font.DisplayMode.NORMAL, 0x0, 0x00F000F0);
            poseStack.popPose();
        }
    }
}
