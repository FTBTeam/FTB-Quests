package dev.ftb.mods.ftbquests.api;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

/**
 * An item filter adapter provides a common interface to wrap the API of external filter mods. Mods can register
 * implementations of this with {@link FTBQuestsAPI.API#registerFilterAdapter(ItemFilterAdapter)}.
 */
public interface ItemFilterAdapter {
    Matcher NO_MATCH = stack -> false;

    /**
     * The human-readable mod name.
     *
     * @return the mod name
     */
    String getName();

    /**
     * Is this item recognized as a filter stack for the implementing mod?
     * @param stack the itemstack to check
     * @return true if it's a filter stack, false otherwise
     */
    boolean isFilterStack(ItemStack stack);

    /**
     * Does the filter represented by the first itemstack match the second itemstack?
     *
     * @param filterStack the itemstack, which should be a filter added by the implementing mod
     * @param toCheck the itemstack to check
     * @return true if the first item is a filter stack AND the second item is matched by it
     */
    boolean doesItemMatch(ItemStack filterStack, ItemStack toCheck);

    Matcher getMatcher(ItemStack filterStack);

    /**
     * Does this filter mod provide a way to filter items by item tag?
     * @return true if the mod has an item tag filter implementation, false otherwise
     */
    default boolean hasItemTagFilter() {
        return true;
    }

    /**
     * Create an itemstack which filters on the given item tag. This will only called if {@link #hasItemTagFilter()}
     * returns true.
     *
     * @return a new filter itemstack which filters by item tag
     */
    ItemStack makeTagFilterStack(TagKey<Item> tag);

    interface Matcher extends Predicate<ItemStack> {
    }
}
