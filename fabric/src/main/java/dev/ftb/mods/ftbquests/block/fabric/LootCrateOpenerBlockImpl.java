package dev.ftb.mods.ftbquests.block.fabric;

import net.minecraft.world.level.block.entity.BlockEntityType;

public class LootCrateOpenerBlockImpl {
    public static BlockEntityType.BlockEntitySupplier<FabricLootCrateOpenerBlockEntity> blockEntityProvider() {
        return FabricLootCrateOpenerBlockEntity::new;
    }
}
