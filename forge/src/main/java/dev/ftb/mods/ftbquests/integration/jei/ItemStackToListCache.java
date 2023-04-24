package dev.ftb.mods.ftbquests.integration.jei;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Function;

/**
 * An LRU cache to quickly map an itemstack (with possible NBT) to a list of something (either wrapped quest or loot
 * crate "recipes"). Important for fast JEI/REI lookup.
 *
 * @param <T> type of recipe being mapped to
 */
public class ItemStackToListCache<T> {
    private static final int MAX_CACHE_SIZE = 1024;

    private final Object2ObjectLinkedOpenCustomHashMap<ItemStack, List<T>> cacheMap
            = new Object2ObjectLinkedOpenCustomHashMap<>(new ItemStackHashingStrategy());

    public List<T> getList(ItemStack stack, Function<ItemStack, List<T>> toCompute) {
        if (cacheMap.containsKey(stack)) {
            return cacheMap.getAndMoveToFirst(stack);
        } else {
            List<T> list = toCompute.apply(stack);
            if (cacheMap.size() == MAX_CACHE_SIZE) {
                cacheMap.removeLast();
            }
            cacheMap.put(stack, list);
            return list;
        }
    }

    public void clear() {
        cacheMap.clear();
    }

    private static class ItemStackHashingStrategy implements Hash.Strategy<ItemStack> {
        @Override
        public int hashCode(ItemStack object) {
            int hashCode = Item.getId(object.getItem());
            if (object.getTag() != null)
                hashCode += 37 * object.getTag().hashCode();
            return hashCode;
        }

        @Override
        public boolean equals(ItemStack o1, ItemStack o2) {
            return (o1 == o2) ||
                    o1 != null && o2 != null
                            && o1.getItem() == o2.getItem()
                            && (o1.getTag() == null || o1.getTag().equals(o2.getTag()));
        }
    }
}
