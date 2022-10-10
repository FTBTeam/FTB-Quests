package dev.ftb.mods.ftbquests.integration.jei;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public enum LootCrateRecipeManagerPlugin implements IRecipeManagerPlugin {
    INSTANCE;

    private final List<WrappedLootCrate> wrappedLootCratesCache = new ArrayList<>();
    private final List<ItemStack> crates = new ArrayList<>();
    private boolean needsRefresh = true;

    List<WrappedLootCrate> getWrappedLootCrates() {
        if (needsRefresh) {
            rebuildWrappedLootCrateCache();
            needsRefresh = false;
        }
        return wrappedLootCratesCache;
    }

    public void refresh() {
        needsRefresh = true;
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

    @Override
    public <V> List<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
        return IRecipeManagerPlugin.super.getRecipeTypes(focus);
    }

    @Override
    public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        if (recipeCategory instanceof LootCrateCategory && focus.getTypedValue().getIngredient() instanceof ItemStack stack) {
            // safe to cast here since we've checked the category
            if (stack.getItem() == FTBQuestsItems.LOOTCRATE.get() && focus.getRole() == RecipeIngredientRole.CATALYST) {
                //noinspection unchecked
                return (List<T>) getWrappedLootCrates();
            }
            return switch (focus.getRole()) {
                case INPUT -> //noinspection unchecked
                        (List<T>) findCratesWithInput(stack);
                case OUTPUT -> //noinspection unchecked
                        (List<T>) findCratesWithOutput(stack);
                default -> Collections.emptyList();
            };
        }
        return Collections.emptyList();
    }

    private List<WrappedLootCrate> findCratesWithInput(ItemStack stack) {
        return getWrappedLootCrates().stream()
                .filter(c -> ItemStack.isSame(c.crateStack, stack))
                .toList();
    }

    private List<WrappedLootCrate> findCratesWithOutput(ItemStack stack) {
        return getWrappedLootCrates().stream()
                .filter(c -> c.outputs.stream().anyMatch(s1 -> ItemStack.isSame(s1, stack)))
                .toList();
    }

    @Override
    public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
        if (recipeCategory instanceof LootCrateCategory) {
            // safe to cast here since we've checked the category
            //noinspection unchecked
            return (List<T>) getWrappedLootCrates();
        }
        return Collections.emptyList();
    }

    @Override
    public <V> List<ResourceLocation> getRecipeCategoryUids(IFocus<V> focus) {
        // TODO 1.19 remove this
        return Collections.singletonList(new ResourceLocation(FTBQuests.MOD_ID, "loot_crate"));
    }

}