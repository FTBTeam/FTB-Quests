package dev.ftb.mods.ftbquests.block.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import dev.ftb.mods.ftbquests.block.entity.LootCrateOpenerBlockEntity;
import dev.ftb.mods.ftbquests.item.LootCrateItem;

public class NeoLootCrateOpenerBlockEntity extends LootCrateOpenerBlockEntity {
    private final LootCrateHandler lootCrateHandler = new LootCrateHandler();

    public NeoLootCrateOpenerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    public ResourceHandler<ItemResource> getLootCrateHandler() {
        return lootCrateHandler;
    }

    private class LootCrateHandler implements ResourceHandler<ItemResource> {
        @Override
        public int size() {
            return NeoLootCrateOpenerBlockEntity.this._getSlots();
        }

        @Override
        public ItemResource getResource(int slot) {
            return ItemResource.of(NeoLootCrateOpenerBlockEntity.this._getStackInSlot(slot));
        }

        @Override
        public long getAmountAsLong(int slot) {
            return NeoLootCrateOpenerBlockEntity.this._getStackInSlot(slot).getCount();
        }

        @Override
        public long getCapacityAsLong(int i, ItemResource resource) {
            return resource.getItem() instanceof LootCrateItem ? Long.MAX_VALUE : 0L;
        }

        @Override
        public boolean isValid(int i, ItemResource resource) {
            return NeoLootCrateOpenerBlockEntity.this._isItemValid(i, resource.toStack());
        }

        @Override
        public int insert(int i, ItemResource resource, int j, TransactionContext transaction) {
//            return NeoForgeLootCrateOpenerBlockEntity.this._insertItem(slot, stack, simulate);
            return 0;
        }

        @Override
        public int extract(int i, ItemResource resource, int j, TransactionContext transaction) {
//            return NeoForgeLootCrateOpenerBlockEntity.this._insertItem(slot, stack, simulate);
            return 0;
        }
    }
}
