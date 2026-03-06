package dev.ftb.mods.ftbquests.neoforge;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.block.neoforge.NeoForgeTaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.client.TaskScreenRenderer;

public class NeoForgeTaskScreenRenderer extends TaskScreenRenderer {
    public NeoForgeTaskScreenRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public AABB getRenderBoundingBox(TaskScreenBlockEntity blockEntity) {
        return blockEntity instanceof NeoForgeTaskScreenBlockEntity be ?
                be.getRenderBoundingBox() :
                super.getRenderBoundingBox(blockEntity);
    }
}
