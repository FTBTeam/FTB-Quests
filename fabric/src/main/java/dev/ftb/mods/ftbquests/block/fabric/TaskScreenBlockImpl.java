package dev.ftb.mods.ftbquests.block.fabric;

import dev.ftb.mods.ftbquests.block.entity.TaskScreenAuxBlockEntity;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class TaskScreenBlockImpl {
    public static BlockEntityType.BlockEntitySupplier<TaskScreenBlockEntity> blockEntityProvider() {
        return FabricTaskScreenBlockEntity::new;
    }

    public static BlockEntityType.BlockEntitySupplier<TaskScreenAuxBlockEntity> blockEntityAuxProvider() {
        return FabricTaskScreenAuxBlockEntity::new;
    }
}
