package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.gui.CustomToast;
import dev.ftb.mods.ftbquests.client.gui.quests.ValidItemsScreen;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.client.Minecraft;
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
            Minecraft.getInstance().getToastManager().addToast(new CustomToast(Component.literal("No valid items!"), ItemIcon.ofItem(ModItems.MISSING_ITEM.get()), Component.literal("Report this bug to modpack author!")));
        } else {
            new ValidItemsScreen(itemTask, validItems, canClick).openGui();
        }
    }
}
