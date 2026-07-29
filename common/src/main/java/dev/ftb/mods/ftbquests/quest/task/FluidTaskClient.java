package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.client.gui.widget.Button;
import dev.ftb.mods.ftblibrary.platform.fluid.FluidStack;
import dev.ftb.mods.ftbquests.FTBQuests;

public enum FluidTaskClient implements TaskClient {
    INSTANCE;

    @Override
    public void onButtonClicked(Task task, Button button, boolean canClick) {
        ensureTaskType(task, FluidTask.class).ifPresent(fluidTask -> {
            if (FTBQuests.getRecipeModHelper().isRecipeModAvailable()) {
                var fluidStack = new FluidStack(fluidTask.getFluid(), fluidTask.getMaxProgress(), fluidTask.getFluidDataComponentPatch());
                FTBQuests.getRecipeModHelper().showRecipes(fluidStack);
            } else {
                TaskClient.super.onButtonClicked(task, button, canClick);
            }
        });
    }
}
