package dev.ftb.mods.ftbquests.integration.item_filtering;

import dev.ftb.mods.ftbquests.api.ItemFilterAdapter;
import dev.ftb.mods.ftbquests.api.event.CustomFilterDisplayItemsEvent;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DisplayStacksCache {
    private static final int MAX_CACHE_SIZE = 1024;
    private static final Object2ObjectLinkedOpenHashMap<CacheKey, List<ItemStack>> cache = new Object2ObjectLinkedOpenHashMap<>(MAX_CACHE_SIZE);

    @NotNull
    public static List<ItemStack> getCachedDisplayStacks(ItemStack filterStack, ItemFilterAdapter adapter) {
        CacheKey key = new CacheKey(filterStack);

        List<ItemStack> result = cache.getAndMoveToFirst(key);
        if (result == null) {
            result = computeMatchingStacks(adapter.getMatcher(filterStack));
            cache.put(key, result);
            if (cache.size() >= MAX_CACHE_SIZE) {
                cache.removeLast();
            }
        }

        return result;
    }

    private static List<ItemStack> computeMatchingStacks(ItemFilterAdapter.Matcher matcher) {
        FTBQuestsClient.registryAccess().ifPresent(ra -> CreativeModeTabs.tryRebuildTabContents(FeatureFlags.DEFAULT_FLAGS, true, ra));

        List<ItemStack> res = CreativeModeTabs.searchTab().getSearchTabDisplayItems().stream()
                .filter(matcher)
                .collect(Collectors.toCollection(ArrayList::new));

        List<ItemStack> extra = new ArrayList<>();
        CustomFilterDisplayItemsEvent.ADD_ITEMSTACK.invoker()
                .accept(new CustomFilterDisplayItemsEvent(extra::add));
        res.addAll(extra.stream().filter(matcher).toList());

        return Collections.unmodifiableList(res);
    }

    public static void clear() {
        cache.clear();
    }

    private static class CacheKey {
        private final int key;

        private CacheKey(ItemStack filterStack) {
            key = Objects.hash(BuiltInRegistries.ITEM.getId(filterStack.getItem()), filterStack.hasTag() ? filterStack.getTag().hashCode() : 0);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return key == cacheKey.key;
        }

        @Override
        public int hashCode() {
            return key;
        }
    }
}
