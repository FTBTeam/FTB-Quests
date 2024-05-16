package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ScreenBlockItem extends BlockItem {
    public enum ScreenSize {
        ONE_X_ONE(1),
        THREE_X_THREE(3),
        FIVE_X_FIVE(5),
        SEVEN_X_SEVEN(7);

        private final int size;

        ScreenSize(int size) {
            this.size = size;
        }

        public int getSize() {
            return size;
        }
    }

    private final ScreenSize size;

    public ScreenBlockItem(Block block, ScreenSize size) {
        super(block, ModItems.defaultProps());

        this.size = size;
    }

    public static int getSize(ItemStack stack) {
        return stack.getItem() instanceof ScreenBlockItem sb ? sb.size.getSize() : 1;
    }
}
