package dev.ftb.mods.ftbquests.integration.jei;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public enum LootCrateRecipeManagerPlugin implements IRecipeManagerPlugin {
    INSTANCE;

    private final List<WrappedLootCrate> wrappedLootCratesCache = new ArrayList<>();
    private final ItemStackToListCache<WrappedLootCrate> inputCache = new ItemStackToListCache<>();
    private final ItemStackToListCache<WrappedLootCrate> outputCache = new ItemStackToListCache<>();
    private final List<ItemStack> crates = new ArrayList<>();
    private boolean needsRefresh = true;

    List<WrappedLootCrate> getWrappedLootCrates() {
        if (needsRefresh) {
            rebuildWrappedLootCrateCache();
            needsRefresh = false;
        }
        return wrappedLootCratesCache;
    }

    public void rebuildWrappedLootCrateCache() {
        if (FTBQuestsJEIIntegration.runtime == null) return;

        if (!crates.isEmpty()) {
            FTBQuestsJEIIntegration.runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, crates);
        }

        wrappedLootCratesCache.clear();
        crates.clear();

        if (ClientQuestFile.exists()) {
            for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables) {
                if (table.lootCrate != null) {
                    WrappedLootCrate wrapper = new WrappedLootCrate(table.lootCrate);
                    wrappedLootCratesCache.add(wrapper);
                    crates.add(table.lootCrate.createStack());
                }
            }
        }

        if (!crates.isEmpty()) {
            FTBQuestsJEIIntegration.runtime.getIngredientManager().addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, crates);
        }
    }

    public void refresh() {
        needsRefresh = true;
        inputCache.clear();
        outputCache.clear();
    }

    @Override
    public <V> List<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
        if (focus.getTypedValue().getIngredient() instanceof ItemStack stack
                && focus.getRole() == RecipeIngredientRole.INPUT
                && stack.getItem() == FTBQuestsItems.LOOTCRATE.get())
        {
            return List.of(JEIRecipeTypes.LOOT_CRATE);
        }

        return List.of();
    }

    @Override
    public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        if (recipeCategory instanceof LootCrateCategory && focus.getTypedValue().getIngredient() instanceof ItemStack stack) {
            if (stack.getItem() == FTBQuestsItems.LOOTCRATE.get() && focus.getRole() == RecipeIngredientRole.CATALYST) {
                // safe to cast here since we've checked the category
                //noinspection unchecked
                return (List<T>) getWrappedLootCrates();
            }
            return switch (focus.getRole()) {
                case INPUT -> //noinspection unchecked
                        (List<T>) findCratesWithInput(stack);
                case OUTPUT -> //noinspection unchecked
                        (List<T>) findCratesWithOutput(stack);
                default -> List.of();
            };
        }
        return List.of();
    }

    @Override
    public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
        // safe to cast here since we've checked the category
        //noinspection unchecked
        return recipeCategory instanceof LootCrateCategory ? (List<T>) getWrappedLootCrates() : List.of();
    }

    private List<WrappedLootCrate> findCratesWithInput(ItemStack stack) {
        return inputCache.getList(stack, k -> getWrappedLootCrates().stream()
                .filter(c -> ItemStack.isSameItemSameTags(c.crateStack, stack))
                .toList()
        );
    }

    private List<WrappedLootCrate> findCratesWithOutput(ItemStack stack) {
        return outputCache.getList(stack, k -> getWrappedLootCrates().stream()
                .filter(c -> c.outputs.stream().anyMatch(s1 -> ItemStack.isSameItemSameTags(s1, stack)))
                .toList()
        );
    }
}