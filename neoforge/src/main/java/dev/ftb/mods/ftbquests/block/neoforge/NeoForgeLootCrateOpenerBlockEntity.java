package dev.ftb.mods.ftbquests.block.neoforge;

import dev.ftb.mods.ftbquests.block.entity.LootCrateOpenerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class NeoForgeLootCrateOpenerBlockEntity extends LootCrateOpenerBlockEntity {
    private final LootCrateHandler lootCrateHandler = new LootCrateHandler();

    public NeoForgeLootCrateOpenerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    public IItemHandler getLootCrateHandler() {
        return lootCrateHandler;
    }

    private class LootCrateHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return NeoForgeLootCrateOpenerBlockEntity.this._getSlots();
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return NeoForgeLootCrateOpenerBlockEntity.this._getStackInSlot(slot);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return NeoForgeLootCrateOpenerBlockEntity.this._insertItem(slot, stack, simulate);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return NeoForgeLootCrateOpenerBlockEntity.this._extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return NeoForgeLootCrateOpenerBlockEntity.this._isItemValid(slot, stack);
        }
    }
}
