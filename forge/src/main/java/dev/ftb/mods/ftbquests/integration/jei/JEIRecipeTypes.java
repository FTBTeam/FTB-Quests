package dev.ftb.mods.ftbquests.integration.jei;

import dev.ftb.mods.ftbquests.FTBQuests;
import mezz.jei.api.recipe.RecipeType;

public class JEIRecipeTypes {
    public static RecipeType<WrappedQuest> QUEST = RecipeType.create(FTBQuests.MOD_ID, "quest", WrappedQuest.class);
    public static RecipeType<WrappedLootCrate> LOOT_CRATE = RecipeType.create(FTBQuests.MOD_ID, "loot_crate", WrappedLootCrate.class);
}
