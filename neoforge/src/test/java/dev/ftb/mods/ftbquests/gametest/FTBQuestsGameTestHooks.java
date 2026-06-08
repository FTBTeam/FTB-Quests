package dev.ftb.mods.ftbquests.gametest;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@EventBusSubscriber(modid = FTBQuestsAPI.MOD_ID)
public final class FTBQuestsGameTestHooks {

	private FTBQuestsGameTestHooks() {}

	@SubscribeEvent
	public static void onRegisterGameTests(RegisterGameTestsEvent event) {
		FTBQuestsGameTests.registerTests(event);
	}
}
