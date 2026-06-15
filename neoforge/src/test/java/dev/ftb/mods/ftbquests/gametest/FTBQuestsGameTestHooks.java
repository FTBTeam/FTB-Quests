package dev.ftb.mods.ftbquests.gametest;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.gametest.base.FTBQGameTest;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = FTBQuestsAPI.MOD_ID)
public final class FTBQuestsGameTestHooks {

	private FTBQuestsGameTestHooks() {}

	@SubscribeEvent
	public static void onRegisterTestInstanceTypes(RegisterEvent event) {
		event.register(Registries.TEST_INSTANCE_TYPE, helper -> helper.register(FTBQuestsAPI.id("inline"), FTBQGameTest.CODEC));
	}

	@SubscribeEvent
	public static void onRegisterGameTests(RegisterGameTestsEvent event) {
		FTBQuestsGameTests.registerTests(event);
	}
}
