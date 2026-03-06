package dev.ftb.mods.ftbquests.block.neoforge;

import net.minecraft.world.level.block.entity.BlockEntityType;

import dev.ftb.mods.ftbquests.block.entity.TaskScreenAuxBlockEntity;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;

public class TaskScreenBlockImpl {
    public static BlockEntityType.BlockEntitySupplier<TaskScreenBlockEntity> blockEntityProvider() {
        return NeoForgeTaskScreenBlockEntity::new;
    }

    public static BlockEntityType.BlockEntitySupplier<TaskScreenAuxBlockEntity> blockEntityAuxProvider() {
        return NeoForgeTaskScreenAuxBlockEntity::new;
    }
}
