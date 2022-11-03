package dev.ftb.mods.ftbquests.block.forge;

import net.minecraft.world.level.block.entity.BlockEntityType;

public class LootCrateOpenerBlockImpl {
    public static BlockEntityType.BlockEntitySupplier<ForgeLootCrateOpenerBlockEntity> blockEntityProvider() {
        return ForgeLootCrateOpenerBlockEntity::new;
    }
}
