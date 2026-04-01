package dev.ftb.mods.ftbquests.fabric;

import dev.ftb.mods.ftbquests.FTBQuestsPlatform;
import dev.ftb.mods.ftbquests.block.entity.*;
import dev.ftb.mods.ftbquests.block.fabric.FabricLootCrateOpenerBlockEntity;
import dev.ftb.mods.ftbquests.block.fabric.FabricTaskScreenAuxBlockEntity;
import dev.ftb.mods.ftbquests.block.fabric.FabricTaskScreenBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class FTBQuestsFabricPlatformImpl implements FTBQuestsPlatform {
    @Override
    public BlockEntityType.BlockEntitySupplier<LootCrateOpenerBlockEntity> lootCrateBlockEntityProvider() {
        return FabricLootCrateOpenerBlockEntity::new;
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<TaskScreenBlockEntity> taskScreenBlockEntityProvider() {
        return FabricTaskScreenBlockEntity::new;
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<TaskScreenAuxBlockEntity> taskScreenAuxBlockEntityProvider() {
        return FabricTaskScreenAuxBlockEntity::new;
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<QuestBarrierBlockEntity> questBarrierBlockEntityProvider() {
        return QuestBarrierBlockEntity::new;
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<StageBarrierBlockEntity> stageBarrierBlockEntityProvider() {
        return StageBarrierBlockEntity::new;
    }
}
