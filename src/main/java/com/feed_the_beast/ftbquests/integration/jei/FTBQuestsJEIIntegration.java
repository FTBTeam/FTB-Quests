package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftbquests.block.BlockProgressScreen;
import com.feed_the_beast.ftbquests.block.BlockTaskScreen;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.tile.TileProgressScreenCore;
import com.feed_the_beast.ftbquests.tile.TileTaskScreenCore;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;

/**
 * @author LatvianModder
 */
@JEIPlugin
public class FTBQuestsJEIIntegration implements IModPlugin
{
	public static IJeiRuntime runtime;
	public static IModRegistry registry;

	@Override
	public void onRuntimeAvailable(IJeiRuntime r)
	{
		runtime = r;
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistry r)
	{
		r.registerSubtypeInterpreter(FTBQuestsItems.LOOTCRATE, stack -> stack.hasTagCompound() ? stack.getTagCompound().getString("type") : "");

		r.registerSubtypeInterpreter(FTBQuestsItems.SCREEN, stack -> {
			if (stack.hasTagCompound())
			{
				TileTaskScreenCore t = BlockTaskScreen.getStatic();
				t.resetData();
				t.readFromItem(stack);
				return Integer.toString(t.size);
			}

			return "";
		});

		r.registerSubtypeInterpreter(FTBQuestsItems.PROGRESS_SCREEN, stack -> {
			if (stack.hasTagCompound())
			{
				TileProgressScreenCore t = BlockProgressScreen.getStatic();
				t.resetData();
				t.readFromItem(stack);
				return t.width + "x" + t.height;
			}

			return "";
		});
	}

	@Override
	public void register(IModRegistry r)
	{
		registry = r;
		r.handleRecipes(QuestWrapper.class, recipe -> recipe, QuestCategory.UID);
		r.addRecipeCatalyst(new ItemStack(FTBQuestsItems.BOOK), QuestCategory.UID);

		r.handleRecipes(LootCrateWrapper.class, recipe -> recipe, LootCrateCategory.UID);
		r.addRecipeCatalyst(new ItemStack(FTBQuestsItems.BOOK), LootCrateCategory.UID);
		r.addRecipeCatalyst(new ItemStack(FTBQuestsItems.LOOTCRATE), LootCrateCategory.UID);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration r)
	{
		r.addRecipeCategories(new QuestCategory(r.getJeiHelpers().getGuiHelper()));
		r.addRecipeCategories(new LootCrateCategory(r.getJeiHelpers().getGuiHelper()));
	}
}