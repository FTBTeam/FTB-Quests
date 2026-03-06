package dev.ftb.mods.ftbquests.integration.item_filtering;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.ItemFilterAdapter;
import dev.ftb.mods.ftbquests.api.event.CustomFilterDisplayItemsEvent;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;

import java.util.List;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class DisplayStacksCache {
    private static final int MAX_CACHE_SIZE = 1024;
    private static final Int2ObjectLinkedOpenHashMap<List<ItemStack>> cache = new Int2ObjectLinkedOpenHashMap<>(MAX_CACHE_SIZE);
    @Nullable
    private static List<ItemStack> extraCache = null;

    public static List<ItemStack> getCachedDisplayStacks(ItemStack filterStack, ItemFilterAdapter adapter, HolderLookup.Provider registryAccess) {
        int key = ItemStack.hashItemAndComponents(filterStack);

        List<ItemStack> result = cache.getAndMoveToFirst(key);
        if (result == null) {
            result = computeMatchingStacks(adapter.getMatcher(filterStack, registryAccess));
            cache.put(key, result);
            if (cache.size() >= MAX_CACHE_SIZE) {
                cache.removeLast();
            }
        }

        return result;
    }

    private static List<ItemStack> computeMatchingStacks(ItemFilterAdapter.Matcher matcher) {
        FTBQuestsClient.creativeTabDisplayParams().ifPresent(params -> {
            if (CreativeModeTabs.tryRebuildTabContents(params.enabledFeatures(), params.hasPermissions(), params.holders())) {
                FTBQuests.LOGGER.debug("creative tabs rebuilt, search tab now has {} items", CreativeModeTabs.searchTab().getSearchTabDisplayItems().size());
            }
        });

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
