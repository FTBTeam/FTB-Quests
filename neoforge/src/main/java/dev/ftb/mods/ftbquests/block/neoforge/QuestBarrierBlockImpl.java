package dev.ftb.mods.ftbquests.block.neoforge;

import dev.ftb.mods.ftbquests.block.entity.QuestBarrierBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class QuestBarrierBlockImpl {
    public static BlockEntityType.BlockEntitySupplier<QuestBarrierBlockEntity> questBlockEntityProvider() {
        return NeoForgeQuestBarrierBlockEntity::new;
    }
}
