package dev.ftb.mods.ftbquests.integration;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

public interface RecipeModHelper {
    void refreshAll(Components component);

    void refreshRecipes(QuestObjectBase object);

    void showRecipes(ItemStack object);

    default boolean isRecipeModAvailable() {
        return false;
    }

    /**
     * For supporting dynamic addition & removal of loot crates at runtime, based on reward table settings
     * @param toRemove items to remove
     * @param toAdd items to add
     */
    default void updateItemsDynamic(Collection<ItemStack> toRemove, Collection<ItemStack> toAdd) {
    }

    String getHelperName();

    enum Components {
        QUESTS,
        LOOT_CRATES
    }

    class NoOp implements RecipeModHelper {
        @Override
        public void refreshAll(Components component) {
        }

        @Override
        public void refreshRecipes(QuestObjectBase object) {
        }

        @Override
        public void showRecipes(ItemStack object) {
        }

        @Override
        public String getHelperName() {
            return "NO-OP";
        }
    }
}
