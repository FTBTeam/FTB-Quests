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

	public final ArrayList<LootCrateEntry> list = new ArrayList<>();

	public void refresh()
	{
		if (FTBQuestsJEIIntegration.RUNTIME != null && !list.isEmpty())
		{
			for (LootCrateEntry entry : list)
			{
				FTBQuestsJEIIntegration.RUNTIME.getRecipeRegistry().removeRecipe(entry, LootCrateCategory.UID);
			}
		}

		list.clear();

		if (ClientQuestFile.exists())
		{
			for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables)
			{
				if (table.lootCrate != null && !table.lootCrate.stringID.isEmpty())
				{
					LootCrateEntry entry = new LootCrateEntry(table.lootCrate);
					list.add(entry);
					FTBQuestsJEIIntegration.RUNTIME.getRecipeRegistry().addRecipe(entry, LootCrateCategory.UID);
				}
			}
		}
	}
}