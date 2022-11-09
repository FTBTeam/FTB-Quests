package dev.ftb.mods.ftbquests.block.fabric;

import dev.ftb.mods.ftbquests.block.entity.LootCrateOpenerBlockEntity;
import dev.ftb.mods.ftbquests.item.LootCrateItem;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("UnstableApiUsage")
public class FabricLootCrateOpenerBlockEntity extends LootCrateOpenerBlockEntity {
    private final Storage<ItemVariant> itemStorage = new ItemStorageHandler();

    public FabricLootCrateOpenerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    public Storage<ItemVariant> getItemStorage() {
        return itemStorage;
    }

    private class ItemStorageHandler extends SingleVariantStorage<ItemVariant> {
        @Override
        protected ItemVariant getBlankVariant() {
            return ItemVariant.blank();
        }

        @Override
        protected long getCapacity(ItemVariant variant) {
            return variant.getItem() instanceof LootCrateItem ? Long.MAX_VALUE : 0L;
        }

        @Override
        public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
            ItemStack stack = insertedVariant.toStack();
            ItemStack excess = FabricLootCrateOpenerBlockEntity.this._insertItem(0, stack, true);
            int inserted = stack.getCount() - excess.getCount();
            if (inserted == 0) return 0L;
            transaction.addCloseCallback((t1, result) -> {
                if (result.wasCommitted()) {
                    FabricLootCrateOpenerBlockEntity.this._insertItem(0, stack, false);
                }
            });
            return inserted;
        }

        @Override
        public long extract(ItemVariant extractedVariant, long maxAmount, TransactionContext transaction) {
            ItemStack toExtract = FabricLootCrateOpenerBlockEntity.this._extractItem(1, (int) maxAmount, true);
            if (!toExtract.isEmpty()) {
                transaction.addCloseCallback((t1, result) -> {
                    if (result.wasCommitted()) {
                        FabricLootCrateOpenerBlockEntity.this._extractItem(1, (int) maxAmount, false);
                    }
                });
            }
            return toExtract.getCount();
        }

        @Override
        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(FabricLootCrateOpenerBlockEntity.this._getStackInSlot(1));
        }
    }
}
