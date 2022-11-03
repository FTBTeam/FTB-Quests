package dev.ftb.mods.ftbquests.block.forge;

import dev.ftb.mods.ftbquests.block.entity.LootCrateOpenerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForgeLootCrateOpenerBlockEntity extends LootCrateOpenerBlockEntity {
    private final LootCrateHandler lootCrateHandler = new LootCrateHandler();
    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(() -> lootCrateHandler);

    public ForgeLootCrateOpenerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();

        itemCap.invalidate();
    }

    private class LootCrateHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return ForgeLootCrateOpenerBlockEntity.this._getSlots();
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ForgeLootCrateOpenerBlockEntity.this._getStackInSlot(slot);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return ForgeLootCrateOpenerBlockEntity.this._insertItem(slot, stack, simulate);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ForgeLootCrateOpenerBlockEntity.this._extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return ForgeLootCrateOpenerBlockEntity.this._isItemValid(slot, stack);
        }
    }
}
