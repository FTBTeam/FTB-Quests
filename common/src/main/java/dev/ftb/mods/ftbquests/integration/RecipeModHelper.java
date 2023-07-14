package dev.ftb.mods.ftbquests.integration;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.world.item.ItemStack;

public interface RecipeModHelper {
    void refreshAll(Components component);

    void refreshRecipes(QuestObjectBase object);

    void showRecipes(ItemStack object);

    default boolean isRecipeModAvailable() {
        return false;
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
