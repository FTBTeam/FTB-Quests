package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;

/**
 * @author LatvianModder
 */
@JEIPlugin
public class FTBQuestsJEIIntegration implements IModPlugin
{
	public static IJeiRuntime RUNTIME;

	@Override
	public void onRuntimeAvailable(IJeiRuntime r)
	{
		RUNTIME = r;
	}

	@Override
	public void register(IModRegistry r)
	{
		r.handleRecipes(LootCrateEntry.class, recipe -> recipe, LootCrateCategory.UID);
		r.addRecipeCatalyst(new ItemStack(FTBQuestsItems.LOOTCRATE), LootCrateCategory.UID);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration r)
	{
		r.addRecipeCategories(new LootCrateCategory(r.getJeiHelpers().getGuiHelper()));
	}

	public static void refresh()
	{
		LootCrateRegistry.INSTANCE.refresh();
	}
}