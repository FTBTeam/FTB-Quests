package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import mezz.jei.api.recipe.IFocus;
import net.minecraftforge.fml.ModList;

/**
 * @author LatvianModder
 */
public class FTBQuestsJEIHelper
{
	public static int QUESTS = 1;
	public static int LOOTCRATES = 2;

	public static void refresh(QuestObjectBase object)
	{
		int i = object.refreshJEI();

		if (i != 0 && ModList.get().isLoaded("jei"))
		{
			if ((i & QUESTS) != 0)
			{
				refreshQuests();
			}

			if ((i & LOOTCRATES) != 0)
			{
				refreshLootcrates();
			}
		}
	}

	private static void refreshQuests()
	{
		QuestRegistry.INSTANCE.refresh();
	}

	private static void refreshLootcrates()
	{
		LootCrateRegistry.INSTANCE.refresh();
	}

	public static void showRecipes(Object object)
	{
		FTBQuestsJEIIntegration.runtime.getRecipesGui().show(FTBQuestsJEIIntegration.runtime.getRecipeManager().createFocus(IFocus.Mode.OUTPUT, object));
	}
}