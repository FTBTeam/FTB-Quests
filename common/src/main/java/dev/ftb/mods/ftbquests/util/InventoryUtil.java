package dev.ftb.mods.ftbquests.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class InventoryUtil {
    @ExpectPlatform
    public static NonNullList<ItemStack> getItemsInInventory(Level level, BlockPos pos, Direction side) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean putItemsInInventory(List<ItemStack> items, Level level, BlockPos pos, Direction side,  boolean clearFirst) {
        throw new AssertionError();
    }
}
