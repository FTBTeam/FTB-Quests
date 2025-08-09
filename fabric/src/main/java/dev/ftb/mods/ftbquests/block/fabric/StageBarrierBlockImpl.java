package dev.ftb.mods.ftbquests.block.fabric;

import dev.ftb.mods.ftbquests.block.entity.StageBarrierBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class StageBarrierBlockImpl {
    public static BlockEntityType.BlockEntitySupplier<StageBarrierBlockEntity> stageBlockEntityProvider() {
        return StageBarrierBlockEntity::new;
    }
}
