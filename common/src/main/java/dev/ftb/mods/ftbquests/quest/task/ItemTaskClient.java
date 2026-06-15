package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.client.gui.widget.Button;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.quests.ValidItemsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public enum ItemTaskClient implements TaskClient {
    INSTANCE;

    @Override
    public void onButtonClicked(Task task, Button button, boolean canClick) {
        if (!(task instanceof ItemTask itemTask)) {
            return;
        }

        button.playClickSound();

        List<ItemStack> validItems = itemTask.getValidDisplayItems();

        if (!itemTask.consumesResources() && validItems.size() == 1 && FTBQuests.getRecipeModHelper().isRecipeModAvailable()) {
            FTBQuests.getRecipeModHelper().showRecipes(validItems.getFirst());
        } else if (validItems.isEmpty()) {
            FTBQuestsClient.showErrorToast(Component.literal("No valid items!"), Component.literal("Report this bug to modpack author!"));
        } else {
            new ValidItemsScreen(itemTask, validItems, canClick).openGui();
        }
    }
}
