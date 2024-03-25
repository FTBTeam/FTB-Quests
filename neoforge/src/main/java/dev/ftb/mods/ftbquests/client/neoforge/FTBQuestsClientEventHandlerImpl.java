package dev.ftb.mods.ftbquests.client.neoforge;

import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.neoforge.NeoForgeTaskScreenRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class FTBQuestsClientEventHandlerImpl {
    public static BlockEntityRendererProvider<TaskScreenBlockEntity> taskScreenRenderer() {
        return NeoForgeTaskScreenRenderer::new;
    }
}
