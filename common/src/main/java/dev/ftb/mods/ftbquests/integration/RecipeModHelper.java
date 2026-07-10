package dev.ftb.mods.ftbquests.integration;

import dev.architectury.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

public interface RecipeModHelper {
    void refreshAll(Components component);

    void refreshRecipes(QuestObjectBase object);

    void showRecipes(ItemStack object);

    void showRecipes(FluidStack fluid);

    default boolean isRecipeModAvailable() {
        return false;
    }

    Tristate toggleBookmark(ItemStack stack);

    /**
     * For supporting dynamic addition & removal of loot crates at runtime, based on reward table settings
     * @param toRemove items to remove
     * @param toAdd items to add
     */
    default void updateItemsDynamic(Collection<ItemStack> toRemove, Collection<ItemStack> toAdd) {
    }

    boolean isBookmarkKey(Key key);

    String getHelperName();

    @ApiStatus.NonExtendable
    default boolean tryToggleBookmark(Button button, Key key, Object ingredient) {
        if (button.isMouseOver() && isBookmarkKey(key) && ingredient instanceof ItemStack stack) {
            var result = toggleBookmark(stack);
            if (!result.isDefault()) {
                Component msg = Component.translatable((result.isTrue() ? "ftbquests.bookmark_added" : "ftbquests.bookmark_removed"), getHelperName());
                FTBQuestsClient.showInfoToast(msg, ItemIcon.getItemIcon(stack), stack.getItem().getDescription());
                return true;
            }
        }
        return false;
    }

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
        public void showRecipes(FluidStack fluid) {
        }

        @Override
        public Tristate toggleBookmark(ItemStack stack) {
            return Tristate.DEFAULT;
        }

        @Override
        public boolean isBookmarkKey(Key key) {
            return false;
        }

        @Override
        public String getHelperName() {
            return "NO-OP";
        }
    }
}
