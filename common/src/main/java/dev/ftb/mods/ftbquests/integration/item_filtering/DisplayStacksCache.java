package dev.ftb.mods.ftbquests.integration.item_filtering;

import com.google.common.collect.ImmutableList;
import dev.ftb.mods.ftbquests.api.ItemFilterAdapter;
import dev.ftb.mods.ftbquests.api.event.CustomFilterDisplayItemsEvent;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DisplayStacksCache {
    private static final int MAX_CACHE_SIZE = 1024;
    private static final Int2ObjectLinkedOpenHashMap<List<ItemStack>> cache = new Int2ObjectLinkedOpenHashMap<>(MAX_CACHE_SIZE);
    private static List<ItemStack> extraCache = null;

    @NotNull
    public static List<ItemStack> getCachedDisplayStacks(ItemStack filterStack, ItemFilterAdapter adapter) {
        int key = ItemStack.hashItemAndComponents(filterStack);

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
        if (CreativeModeTabs.searchTab().getSearchTabDisplayItems().isEmpty()) {
            FTBQuestsClient.registryAccess().ifPresent(ra -> CreativeModeTabs.tryRebuildTabContents(FeatureFlags.DEFAULT_FLAGS, true, ra));
        }

        ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();

        CreativeModeTabs.searchTab().getSearchTabDisplayItems().stream()
                .filter(matcher)
                .forEach(builder::add);

        getExtraDisplayCache().stream()
                .filter(matcher)
                .forEach(builder::add);

        return builder.build();
    }

    public static void clear() {
        cache.clear();
        extraCache = null;
    }

    @NotNull
    private static List<ItemStack> getExtraDisplayCache() {
        if (extraCache == null) {
            ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
            CustomFilterDisplayItemsEvent.ADD_ITEMSTACK.invoker()
                    .accept(new CustomFilterDisplayItemsEvent(builder::add));
            extraCache = builder.build();
        }
        return extraCache;
    }
}
