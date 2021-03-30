package dev.ftb.mods.ftbquests.integration.jei;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

/**
 * @author LatvianModder
 */
public enum LootCrateRegistry {
	INSTANCE;

	public final ArrayList<LootCrateWrapper> list = new ArrayList<>();
	public final ArrayList<ItemStack> crates = new ArrayList<>();

	@SuppressWarnings("deprecation")
	public void refresh() {
		/* FIXME: JEI
		if (FTBQuestsJEIIntegration.runtime != null && !list.isEmpty())
		{
			for (LootCrateWrapper wrapper : list)
			{
				//FTBQuestsJEIIntegration.runtime.getRecipeRegistry().removeRecipe(wrapper, LootCrateCategory.UID);
			}
		}

		if (!crates.isEmpty())
		{
			FTBQuestsJEIIntegration.runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM, crates);
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
					FTBQuestsJEIIntegration.runtime.getRecipeManager().addRecipe(wrapper, LootCrateCategory.UID);
				}
			}
		}

		if (!crates.isEmpty())
		{
			FTBQuestsJEIIntegration.runtime.getIngredientManager().addIngredientsAtRuntime(VanillaTypes.ITEM, crates);
		}
		*/
	}
}