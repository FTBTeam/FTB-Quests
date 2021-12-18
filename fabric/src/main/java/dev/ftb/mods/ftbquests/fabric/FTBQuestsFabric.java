package dev.ftb.mods.ftbquests.fabric;

import dev.ftb.mods.ftbquests.FTBQuests;
import net.fabricmc.api.ModInitializer;

public class FTBQuestsFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		new FTBQuests().setup();
	}
}
