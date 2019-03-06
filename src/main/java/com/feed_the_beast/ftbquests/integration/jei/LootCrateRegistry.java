package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

/**
 * @author LatvianModder
 */
public enum LootCrateRegistry
{
	INSTANCE;

	public final ArrayList<LootCrateWrapper> list = new ArrayList<>();
	public final ArrayList<ItemStack> crates = new ArrayList<>();

	@SuppressWarnings("deprecation")
	public void refresh()
	{
		if (FTBQuestsJEIIntegration.runtime != null && !list.isEmpty())
		{
			for (LootCrateWrapper wrapper : list)
			{
				FTBQuestsJEIIntegration.runtime.getRecipeRegistry().removeRecipe(wrapper, LootCrateCategory.UID);
			}
		}

		if (!crates.isEmpty())
		{
			FTBQuestsJEIIntegration.registry.getIngredientRegistry().removeIngredientsAtRuntime(VanillaTypes.ITEM, crates);
		}

		list.clear();
		crates.clear();

		if (ClientQuestFile.exists())
		{
			for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables)
			{
				if (table.lootCrate != null)
				{
					LootCrateWrapper wrapper = new LootCrateWrapper(table.lootCrate);
					list.add(wrapper);
					crates.add(table.lootCrate.createStack());
					FTBQuestsJEIIntegration.runtime.getRecipeRegistry().addRecipe(wrapper, LootCrateCategory.UID);
				}
			}
		}

		if (!crates.isEmpty())
		{
			FTBQuestsJEIIntegration.registry.getIngredientRegistry().addIngredientsAtRuntime(VanillaTypes.ITEM, crates);
		}
	}
}