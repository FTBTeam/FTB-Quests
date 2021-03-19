package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import me.shedaniel.architectury.annotations.ExpectPlatform;

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
	public static void showRecipes(Object object) {
		throw new AssertionError();
	}
}
