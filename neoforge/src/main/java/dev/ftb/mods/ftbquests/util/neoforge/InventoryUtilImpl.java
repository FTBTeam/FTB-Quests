package dev.ftb.mods.ftbquests.util.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

public class InventoryUtilImpl {
    public static NonNullList<ItemStack> getItemsInInventory(Level level, BlockPos pos, Direction side) {
        ResourceHandler<ItemResource> capability = level.getCapability(Capabilities.Item.BLOCK, pos, side);
        NonNullList<ItemStack> items = NonNullList.create();
        if (capability == null) {
            return items;
        }

        for (int i = 0; i < capability.size(); i++) {
            ItemStack stack = capability.getResource(i).toStack();
            if (!stack.isEmpty()) {
                items.add(stack);
            }
        }

        return items;
    }

    public static boolean putItemsInInventory(List<ItemStack> items, Level level, BlockPos pos, Direction side, boolean clearFirst) {
        ResourceHandler<ItemResource> capability = level.getCapability(Capabilities.Item.BLOCK, pos, side);
        if (capability == null) {
            // TODO: @since 21.11: Is throwing really correct here?
            throw new IllegalArgumentException("No item handler at that blockpos & side");
        }

        try (Transaction transaction = Transaction.openRoot()) {
            if (clearFirst) {
                try (Transaction innerTransaction = Transaction.open(transaction)) {
                    for (int i = 0; i < capability.size(); i++) {
                        capability.extract(capability.getResource(i), Integer.MAX_VALUE, transaction);
                    }

                    innerTransaction.commit();
                }
            }

            try (Transaction innerTransaction = Transaction.open(transaction)) {
                for (ItemStack stack : items) {
                    int amountInserted = capability.insert(ItemResource.of(stack), stack.getCount(), innerTransaction);
                    // If not all items could be inserted, abort
                    if (amountInserted < stack.getCount()) {
                        // Abort inner transaction and outer transaction
                        innerTransaction.close();
                        transaction.close();
                        return false;
                    }
                }

                innerTransaction.commit();
            }

            // All items inserted successfully
            transaction.commit();
        }

        return true;
    }
}
