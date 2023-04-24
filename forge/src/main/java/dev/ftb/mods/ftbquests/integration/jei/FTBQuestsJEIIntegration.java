package dev.ftb.mods.ftbquests.integration.jei;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class FTBQuestsJEIIntegration implements IModPlugin {
	private static final ResourceLocation UID = new ResourceLocation(FTBQuests.MOD_ID, "jei");
	public static IJeiRuntime runtime;

	public static void showRecipes(ItemStack stack) {
		if (runtime != null) {
			runtime.getRecipesGui().show(runtime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.OUTPUT, runtime.getIngredientManager().getIngredientType(stack), stack));
		}
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime r) {
		runtime = r;
	}

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration r) {
		r.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, FTBQuestsItems.LOOTCRATE.get(),
				(stack, uidContext) -> stack.hasTag() ? stack.getTag().getString("type") : "");
	}

	@Override
	public void registerRecipes(IRecipeRegistration r) {
		// NOTE: doing nothing here since quest and loot crate "recipes" are dynamic,
		//   and handled by custom recipe manager plugins
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(FTBQuestsItems.BOOK.get()), JEIRecipeTypes.QUEST);
		registration.addRecipeCatalyst(new ItemStack(FTBQuestsItems.LOOTCRATE.get()), JEIRecipeTypes.LOOT_CRATE);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration r) {
		r.addRecipeCategories(new QuestCategory(r.getJeiHelpers().getGuiHelper()));
		r.addRecipeCategories(new LootCrateCategory(r.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerAdvanced(IAdvancedRegistration registration) {
		registration.addRecipeManagerPlugin(QuestRecipeManagerPlugin.INSTANCE);
		registration.addRecipeManagerPlugin(LootCrateRecipeManagerPlugin.INSTANCE);
	}
}
