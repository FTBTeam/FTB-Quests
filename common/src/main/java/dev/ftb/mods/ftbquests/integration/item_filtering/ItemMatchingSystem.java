package dev.ftb.mods.ftbquests.integration.item_filtering;

import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.api.ItemFilterAdapter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public enum ItemMatchingSystem {
    INSTANCE;

    private final List<ItemFilterAdapter> adapters = new CopyOnWriteArrayList<>();

    public void registerFilterAdapter(ItemFilterAdapter adapter) {
        adapters.add(adapter);
    }

    public boolean isItemFilter(ItemStack stack) {
        return getFilterAdapter(stack).isPresent();
    }

    public Optional<ItemFilterAdapter> getFilterAdapter(ItemStack stack) {
        return adapters.stream().filter(adapter -> adapter.isFilterStack(stack)).findFirst();
    }

    public boolean doesItemMatch(ItemStack filterStack, ItemStack toCheck, ComponentMatchType matchType, HolderLookup.Provider registryAccess) {
        return getFilterAdapter(filterStack)
                .map(adapter -> adapter.doesItemMatch(filterStack, toCheck, registryAccess))
                .orElse(areItemStacksEqual(filterStack, toCheck,  matchType));
    }

    public List<ItemStack> getAllMatchingStacks(ItemStack filterStack, HolderLookup.Provider registryAccess) {
        List<ItemStack> res = new ArrayList<>();

        adapters.forEach(adapter -> {
            if (adapter.isFilterStack(filterStack)) {
                res.addAll(DisplayStacksCache.getCachedDisplayStacks(filterStack, adapter, registryAccess));
            }
        });

        return res.isEmpty() ? List.of(filterStack) : res;
    }

    private boolean areItemStacksEqual(ItemStack stackA, ItemStack stackB, ComponentMatchType matchType) {
        if (stackA == stackB) {
            return true;
        } else if (stackA.getItem() != stackB.getItem()) {
            return false;
        } else {
            return switch (matchType) {
                case NONE -> true;
                case FUZZY -> fuzzyMatch(stackA.getComponents(), stackB.getComponents());
                case STRICT -> ItemStack.isSameItemSameComponents(stackA, stackB);
            };
        }
    }

    private boolean fuzzyMatch(DataComponentMap map, DataComponentMap toMatch) {
        return map.stream().allMatch(tc -> toMatch.has(tc.type()) && toMatch.get(tc.type()).equals(tc.value()));
    }

    public Collection<ItemFilterAdapter> adapters() {
        return Collections.unmodifiableCollection(adapters);
    }

    public enum ComponentMatchType {
        NONE("none"),
        FUZZY("fuzzy"),
        STRICT("strict");

        public static final NameMap<ComponentMatchType> NAME_MAP = NameMap.of(NONE, values())
                .id(v -> v.name)
                .create();

        private final String name;

        ComponentMatchType(String name) {
            this.name = name;
        }
    }
}
