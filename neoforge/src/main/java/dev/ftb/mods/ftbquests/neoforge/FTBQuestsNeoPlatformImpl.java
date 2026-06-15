package dev.ftb.mods.ftbquests.neoforge;

import dev.ftb.mods.ftbquests.FTBQuestsPlatform;
import dev.ftb.mods.ftbquests.block.entity.*;
import dev.ftb.mods.ftbquests.block.neoforge.*;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class FTBQuestsNeoPlatformImpl implements FTBQuestsPlatform {
    @Override
    public BlockEntityType.BlockEntitySupplier<LootCrateOpenerBlockEntity> lootCrateBlockEntityProvider() {
        return NeoLootCrateOpenerBlockEntity::new;
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<TaskScreenBlockEntity> taskScreenBlockEntityProvider() {
        return NeoTaskScreenBlockEntity::new;
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<TaskScreenAuxBlockEntity> taskScreenAuxBlockEntityProvider() {
        return NeoTaskScreenAuxBlockEntity::new;
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<QuestBarrierBlockEntity> questBarrierBlockEntityProvider() {
        return NeoQuestBarrierBlockEntity::new;
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<StageBarrierBlockEntity> stageBarrierBlockEntityProvider() {
        return NeoStageBarrierBlockEntity::new;
    }
}
