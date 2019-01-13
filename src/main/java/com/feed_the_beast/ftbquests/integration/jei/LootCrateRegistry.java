package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;

import java.util.ArrayList;

/**
 * @author LatvianModder
 */
public enum LootCrateRegistry
{
	INSTANCE;

	public final ArrayList<LootCrateWrapper> list = new ArrayList<>();

	@SuppressWarnings("deprecation")
	public void refresh()
	{
		if (FTBQuestsJEIIntegration.RUNTIME != null && !list.isEmpty())
		{
			for (LootCrateWrapper wrapper : list)
			{
				FTBQuestsJEIIntegration.RUNTIME.getRecipeRegistry().removeRecipe(wrapper, LootCrateCategory.UID);
			}
		}

		list.clear();

		if (ClientQuestFile.exists())
		{
			for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables)
			{
				if (table.lootCrate != null && !table.lootCrate.stringID.isEmpty())
				{
					LootCrateWrapper wrapper = new LootCrateWrapper(table.lootCrate);
					list.add(wrapper);
					FTBQuestsJEIIntegration.RUNTIME.getRecipeRegistry().addRecipe(wrapper, LootCrateCategory.UID);
				}
			}
		}
	}
}