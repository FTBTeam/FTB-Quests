package dev.ftb.mods.ftbquests.integration.fabric;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static dev.ftb.mods.ftbquests.integration.FTBQuestsJEIHelper.LOOTCRATES;
import static dev.ftb.mods.ftbquests.integration.FTBQuestsJEIHelper.QUESTS;

/**
 * @author LatvianModder
 */
public class FTBQuestsJEIHelperImpl {
	public static Consumer<ItemStack> view = o -> {};

	public static void refresh(QuestObjectBase object) {
		int i = object == null ? 0 : object.refreshJEI();

		if (i != 0 && FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) {
			if ((i & QUESTS) != 0) {
				refreshQuests();
			}

			if ((i & LOOTCRATES) != 0) {
				refreshLootcrates();
			}
		}
	}

	private static void refreshQuests() {
	}

	private static void refreshLootcrates() {
	}

	public static void showRecipes(ItemStack object) {
		view.accept(object);
	}
}
