package dev.ftb.mods.ftbquests.client.fabric;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.client.TaskScreenRenderState;
import dev.ftb.mods.ftbquests.client.TaskScreenRenderer;

public class FTBQuestsClientEventHandlerImpl {
    public static BlockEntityRendererProvider<TaskScreenBlockEntity, TaskScreenRenderState> taskScreenRenderer() {
        return TaskScreenRenderer::new;
    }
}
