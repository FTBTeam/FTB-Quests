package dev.ftb.mods.ftbquests.block.neoforge;

import dev.ftb.mods.ftbquests.block.entity.TaskScreenAuxBlockEntity;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class TaskScreenBlockImpl {
    public static BlockEntityType.BlockEntitySupplier<TaskScreenBlockEntity> blockEntityProvider() {
        return NeoForgeTaskScreenBlockEntity::new;
    }

    public static BlockEntityType.BlockEntitySupplier<TaskScreenAuxBlockEntity> blockEntityAuxProvider() {
        return NeoForgeTaskScreenAuxBlockEntity::new;
    }
}
