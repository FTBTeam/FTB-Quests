package dev.ftb.mods.ftbquests.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.platform.Platform;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.world.item.ItemStack;

public class FTBQuestsJEIHelper {
	public static int QUESTS = 1;
	public static int LOOTCRATES = 2;

	@ExpectPlatform
	public static void refresh(QuestObjectBase object) {
		throw new AssertionError();
	}

	@ExpectPlatform
	private static void refreshQuests() {
		throw new AssertionError();
	}

	@ExpectPlatform
	private static void refreshLootcrates() {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void showRecipes(ItemStack object) {
		throw new AssertionError();
	}

	public static boolean isRecipeModAvailable() {
		return Platform.isFabric() && Platform.isModLoaded("roughlyenoughitems")
				|| Platform.isForge() && Platform.isModLoaded("jei");
	}
}
