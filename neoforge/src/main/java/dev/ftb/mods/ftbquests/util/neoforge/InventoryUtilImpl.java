package dev.ftb.mods.ftbquests.util.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.List;

public class InventoryUtilImpl {
    public static NonNullList<ItemStack> getItemsInInventory(Level level, BlockPos pos, Direction side) {
        // TODO: @since 21.11: Come back to this
//        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
//        NonNullList<ItemStack> items = NonNullList.create();
//
//        if (handler != null) {
//            for (int i = 0; i < handler.getSlots(); i++) {
//                ItemStack stack = handler.getStackInSlot(i);
//                if (!stack.isEmpty()) {
//                    items.add(stack);
//                }
//            }
//        }

        return NonNullList.create();// items;
    }

    public static boolean putItemsInInventory(List<ItemStack> items, Level level, BlockPos pos, Direction side, boolean clearFirst) {
        // TODO: @since 21.11: Come back to this
//        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
//        if (handler == null) {
//            throw new IllegalArgumentException("No item handler at that blockpos & side");
//        }
//
//        if (clearFirst) {
//            for (int i = 0; i < handler.getSlots(); i++) {
//                handler.extractItem(i, Integer.MAX_VALUE, false);
//            }
//        }
//        for (ItemStack stack : items) {
//            ItemStack excess = ItemHandlerHelper.insertItem(handler, stack.copy(), false);
//            if (!excess.isEmpty()) {
//                return false;
//            }
//        }

        return true;
    }
}
