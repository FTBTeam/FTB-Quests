package dev.ftb.mods.ftbquests.integration.jei.forge;

import dev.ftb.mods.ftbquests.integration.jei.FTBQuestsJEIIntegration;
import dev.ftb.mods.ftbquests.integration.jei.LootCrateRegistry;
import dev.ftb.mods.ftbquests.integration.jei.QuestRegistry;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import mezz.jei.api.recipe.IFocus;
import net.minecraftforge.fml.ModList;

import static dev.ftb.mods.ftbquests.integration.jei.FTBQuestsJEIHelper.LOOTCRATES;
import static dev.ftb.mods.ftbquests.integration.jei.FTBQuestsJEIHelper.QUESTS;

/**
 * @author LatvianModder
 */
public class FTBQuestsJEIHelperImpl {
	public static void refresh(QuestObjectBase object) {
		int i = object.refreshJEI();

		if (i != 0 && ModList.get().isLoaded("jei")) {
			if ((i & QUESTS) != 0) {
				refreshQuests();
			}

			if ((i & LOOTCRATES) != 0) {
				refreshLootcrates();
			}
		}
	}

	private static void refreshQuests() {
		QuestRegistry.INSTANCE.refresh();
	}

	private static void refreshLootcrates() {
		LootCrateRegistry.INSTANCE.refresh();
	}

	public static void showRecipes(Object object) {
		FTBQuestsJEIIntegration.runtime.getRecipesGui().show(FTBQuestsJEIIntegration.runtime.getRecipeManager().createFocus(IFocus.Mode.OUTPUT, object));
	}
}