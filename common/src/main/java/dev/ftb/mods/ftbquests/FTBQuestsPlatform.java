package dev.ftb.mods.ftbquests;

import dev.ftb.mods.ftbquests.block.entity.*;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ServiceLoader;

public interface FTBQuestsPlatform {
    FTBQuestsPlatform INSTANCE = ServiceLoader.load(FTBQuestsPlatform.class).findFirst().orElseThrow();

    static FTBQuestsPlatform get() {
        return INSTANCE;
    }

    BlockEntityType.BlockEntitySupplier<LootCrateOpenerBlockEntity> lootCrateBlockEntityProvider();

    BlockEntityType.BlockEntitySupplier<TaskScreenBlockEntity> taskScreenBlockEntityProvider();

    BlockEntityType.BlockEntitySupplier<TaskScreenAuxBlockEntity> taskScreenAuxBlockEntityProvider();

    BlockEntityType.BlockEntitySupplier<QuestBarrierBlockEntity> questBarrierBlockEntityProvider();

    BlockEntityType.BlockEntitySupplier<StageBarrierBlockEntity> stageBarrierBlockEntityProvider();
}
