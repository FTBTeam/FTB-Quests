package dev.ftb.mods.ftbquests.block.neoforge;

import net.minecraft.world.level.block.entity.BlockEntityType;

public class LootCrateOpenerBlockImpl {
    public static BlockEntityType.BlockEntitySupplier<NeoForgeLootCrateOpenerBlockEntity> blockEntityProvider() {
        return NeoForgeLootCrateOpenerBlockEntity::new;
    }
}
