package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

/**
 * @author LatvianModder
 */
@JeiPlugin
public class FTBQuestsJEIIntegration implements IModPlugin
{
	private static final ResourceLocation UID = new ResourceLocation(FTBQuests.MOD_ID, "jei");
	public static IJeiRuntime runtime;

	@Override
	public void onRuntimeAvailable(IJeiRuntime r)
	{
		runtime = r;
	}

	@Override
	public ResourceLocation getPluginUid()
	{
		return UID;
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration r)
	{
		r.registerSubtypeInterpreter(FTBQuestsItems.LOOTCRATE, stack -> stack.hasTag() ? stack.getTag().getString("type") : "");
	}

	@Override
	public void registerRecipes(IRecipeRegistration r)
	{
		//r.handleRecipes(QuestWrapper.class, recipe -> recipe, QuestCategory.UID);
		//r.addRecipeCatalyst(new ItemStack(FTBQuestsItems.BOOK), QuestCategory.UID);

		//r.handleRecipes(LootCrateWrapper.class, recipe -> recipe, LootCrateCategory.UID);
		//r.addRecipeCatalyst(new ItemStack(FTBQuestsItems.BOOK), LootCrateCategory.UID);
		//r.addRecipeCatalyst(new ItemStack(FTBQuestsItems.LOOTCRATE), LootCrateCategory.UID);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration r)
	{
		//r.addRecipeCategories(new QuestCategory(r.getJeiHelpers().getGuiHelper()));
		//r.addRecipeCategories(new LootCrateCategory(r.getJeiHelpers().getGuiHelper()));
	}
}