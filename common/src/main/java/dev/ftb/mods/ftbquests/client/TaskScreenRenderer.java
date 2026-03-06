package dev.ftb.mods.ftbquests.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import dev.architectury.hooks.client.fluid.ClientFluidStackHooks;

import dev.ftb.mods.ftblibrary.icon.AtlasSpriteIcon;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.block.TaskScreenBlock;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.client.TaskScreenRenderState.ResourceSprite;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.EnergyTask;
import dev.ftb.mods.ftbquests.quest.task.FluidTask;
import dev.ftb.mods.ftbquests.quest.task.Task;

import org.jspecify.annotations.Nullable;

public class TaskScreenRenderer implements BlockEntityRenderer<TaskScreenBlockEntity, TaskScreenRenderState> {
    public static final Identifier INPUT_ONLY_TEXTURE = FTBQuestsAPI.id("tasks/input_only");
    public static final Identifier TANK_TEXTURE = FTBQuestsAPI.id("tasks/tank");
    public static final Identifier FE_ENERGY_EMPTY_TEXTURE = FTBQuestsAPI.id("tasks/fe_empty");
    public static final Identifier FE_ENERGY_FULL_TEXTURE = FTBQuestsAPI.id("tasks/fe_full");
    public static final Identifier TR_ENERGY_EMPTY_TEXTURE = FTBQuestsAPI.id("tasks/ic2_empty");
    public static final Identifier TR_ENERGY_FULL_TEXTURE = FTBQuestsAPI.id("tasks/ic2_full");

    private final ItemModelResolver itemModelResolver;

    public TaskScreenRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public TaskScreenRenderState createRenderState() {
        return new TaskScreenRenderState();
    }

    @Override
    public void extractRenderState(TaskScreenBlockEntity blockEntity, TaskScreenRenderState renderState, float f, Vec3 vec3, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, f, vec3, crumblingOverlay);

        TeamData data = ClientQuestFile.getInstance().getNullableTeamData(blockEntity.getTeamId());
        Task task = blockEntity.getTask();

        if (task == null || data == null) {
            renderState.shouldRender = false;
            return;
        }

        renderState.shouldRender = true;
        renderState.isInputOnly = blockEntity.isInputOnly();
        renderState.teamId = blockEntity.getTeamId();
        itemModelResolver.updateForTopItem(renderState.taskItem, renderState.isInputOnly ? blockEntity.getInputModeIcon() : blockEntity.getTaskItem(), ItemDisplayContext.FIXED, blockEntity.getLevel(), null, (int) blockEntity.getBlockPos().asLong());
        renderState.fakeTextureUV = blockEntity.getFakeTextureUV();
        renderState.textHasShadow = blockEntity.isTextShadow();
        renderState.taskName = task.getTitle();
        renderState.questName = task.getQuest().getTitle();
        long progress = data.getProgress(task);
        if (!task.hideProgressNumbers()) {
            ChatFormatting col = progress > 0 ? (progress >= task.getMaxProgress() ? ChatFormatting.GREEN : ChatFormatting.YELLOW) : ChatFormatting.GOLD;
            renderState.progressText = Component.literal(progress + " / " + task.getMaxProgress()).withStyle(col);
        }
        renderState.interpolatedProgress = (float) progress / task.getMaxProgress();
        if (task instanceof FluidTask fluidTask && fluidTask.getIcon() instanceof AtlasSpriteIcon as && FTBQuestsClientEventHandler.tankSprite != null) {
            renderState.resourceSprite = new ResourceSprite(Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(as.getSpriteId()), true);
            renderState.overlaySprite = new ResourceSprite(FTBQuestsClientEventHandler.tankSprite, false);
            renderState.resourceSpriteTint = ClientFluidStackHooks.getColor(fluidTask.getFluid()) | 0xFF000000;
        } else if (task instanceof EnergyTask energyTask) {
            renderState.resourceSprite = new ResourceSprite(energyTask.getClientData().getEmptyTexture(), false);
            renderState.overlaySprite = new ResourceSprite(energyTask.getClientData().getFullTexture(), true);
        } else {
            renderState.resourceSprite = renderState.overlaySprite = null;
        }
    }

    @Override
    public void submit(TaskScreenRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (!renderState.shouldRender || !(renderState.blockState.getBlock() instanceof TaskScreenBlock taskScreenBlock)) return;

        poseStack.pushPose();

        poseStack.translate(0.5D, 0.5D, 0.5D);
        float rotation = renderState.blockState.getValue(WallSignBlock.FACING).toYRot() + 180f;
        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        int screenSize = taskScreenBlock.getSize() / 2;
        poseStack.translate(-screenSize, -screenSize * 2F, -0.02F);
        poseStack.scale(screenSize * 2F + 1F, screenSize * 2F + 1F, 1F);

        double iconY = 0.5D;

        // render quest and task title at top of screen
        Component top1 = renderState.isInputOnly ? Component.empty() : renderState.questName;
        Component top2 = renderState.isInputOnly ? Component.empty() : renderState.taskName;
        drawString(renderState, submitNodeCollector, poseStack, top1, 0.02D, 0.15F);
        if (!top2.equals(Component.empty())) {
            drawString(renderState, submitNodeCollector, poseStack, top2, 0.17D, 0.07F);
            iconY = 0.54D;
        }

        // render progress numbers at bottom of screen
        if (!renderState.isInputOnly) {
            drawString(renderState, submitNodeCollector, poseStack, renderState.progressText, 0.83D, 0.15F);
        }

        // render icons/sprites for task item/fluid/energy in the middle
        drawTaskIcon(renderState, poseStack, submitNodeCollector, iconY, screenSize);

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.text(Sheets.BLOCKS_MAPPER.sheet()), ((pose, consumer) -> {
            // render skin, if any
            float[] uvs = renderState.fakeTextureUV;
            if (uvs != null && uvs.length == 4) {
                pose.scale(1 / 16f, 1 / 16f, 1 / 16f);
                pose.translate(0, 0, 0.02f);
                RenderUtil.create(pose, consumer, 0f, 0f).withUV(uvs[0], uvs[1], uvs[2], uvs[3]).draw();
            }

            // render input-only texture, if any
            if (renderState.isInputOnly && FTBQuestsClientEventHandler.inputOnlySprite != null) {
                TextureAtlasSprite s = FTBQuestsClientEventHandler.inputOnlySprite;
                RenderUtil.create(pose, consumer, 0, 0).withUV(s.getU0(), s.getV0(), s.getU1(), s.getV1()).draw();
            }
        }));

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public boolean shouldRender(TaskScreenBlockEntity blockEntity, Vec3 vec3) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, vec3);
    }

    private void drawTaskIcon(TaskScreenRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, double iconY, int size) {
        poseStack.pushPose();
        poseStack.translate(0.5D, iconY, -0.0D);
        poseStack.scale(renderState.isInputOnly ? 0.5f : 0.45f, renderState.isInputOnly ? 0.5f : 0.45f, 1f);//0.2f * Math.max(3, size));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));

        if (renderState.resourceSprite != null && renderState.overlaySprite != null) {
            // min z-scale multiplier of 3 is anti z-fighting measure
            poseStack.scale(1 / 16f, 1 / 16f, 0.2f * Math.max(3, size));
            float resourceProgress = renderState.resourceSprite.interpolateHeight() ? renderState.interpolatedProgress : 1f;
            float overlayProgress = renderState.overlaySprite.interpolateHeight() ? renderState.interpolatedProgress : 1f;
            TextureAtlasSprite sprite = renderState.resourceSprite.sprite();
            TextureAtlasSprite overlay = renderState.overlaySprite.sprite();
            collector.submitCustomGeometry(poseStack, RenderTypes.text(sprite.atlasLocation()), ((pose, consumer) -> {
                // fluid/energy texture (interpolated according to task progress)
                if (resourceProgress > 0F) {
                    RenderUtil.create(pose, consumer, -8f, -8f)
                            .withColor(renderState.resourceSpriteTint)
                            .withSize(16f, resourceProgress * 16f)
                            .withUV(sprite.getU0(), sprite.getV0(),
                                    sprite.getU1(), sprite.getV(resourceProgress))
                            .draw();
                }
                // tank overlay texture
                if (overlayProgress > 0) {
                    RenderUtil.create(pose, consumer, -8f, -8f)
                            .withZOffset(-0.0001F)
                            .withSize(16f, overlayProgress * 16f)
                            .withUV(overlay.getU0(), overlay.getV0(),
                                    overlay.getU1(), overlay.getV(overlayProgress))
                            .draw();
                }
            }));
        } else if (!renderState.taskItem.isEmpty()) {
            poseStack.scale(1f, 1f, 0.1f * size);
            renderState.taskItem.submit(poseStack, collector, RenderUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        }

        poseStack.popPose();
    }

    private void drawString(TaskScreenRenderState taskScreen, SubmitNodeCollector collector, PoseStack poseStack, Component text, double y, float size) {
        if (!text.equals(Component.empty())) {
            poseStack.pushPose();
            poseStack.translate(0.5D, y, 0D);

            int len = Minecraft.getInstance().font.width(text);
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
            if (taskScreen.textHasShadow) {
                collector.submitText(poseStack, -len / 2f, 0, text.getVisualOrderText(), true, Font.DisplayMode.NORMAL, RenderUtil.FULL_BRIGHT, 0xFFD8D8D8, 0, 0);
            }
            collector.submitText(poseStack, -len / 2f, 0, text.getVisualOrderText(), false, Font.DisplayMode.POLYGON_OFFSET, RenderUtil.FULL_BRIGHT, 0xFFD8D8D8, 0, 0);
            poseStack.popPose();
        }
    }
}
