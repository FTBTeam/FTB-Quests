package dev.ftb.mods.ftbquests.block.neoforge;

import net.minecraft.world.level.block.entity.BlockEntityType;

import dev.ftb.mods.ftbquests.block.entity.QuestBarrierBlockEntity;

public class QuestBarrierBlockImpl {
    public static BlockEntityType.BlockEntitySupplier<QuestBarrierBlockEntity> questBlockEntityProvider() {
        return NeoForgeQuestBarrierBlockEntity::new;
    }
}
