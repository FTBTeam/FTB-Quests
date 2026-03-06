package dev.ftb.mods.ftbquests.block.fabric;

import net.minecraft.world.level.block.entity.BlockEntityType;

import dev.ftb.mods.ftbquests.block.entity.StageBarrierBlockEntity;

public class StageBarrierBlockImpl {
    public static BlockEntityType.BlockEntitySupplier<StageBarrierBlockEntity> stageBlockEntityProvider() {
        return StageBarrierBlockEntity::new;
    }
}
