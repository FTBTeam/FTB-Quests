package dev.ftb.mods.ftbquests.gametest;

import dev.ftb.mods.ftbquests.gametest.base.FTBQTestRegistrar;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public final class FTBQuestsGameTests {

	private FTBQuestsGameTests() {}

	public static void registerTests(RegisterGameTestsEvent event) {
		FTBQTestRegistrar registrar = new FTBQTestRegistrar(event);

		ProgressionTests.register(registrar);
		ResetTests.register(registrar);
		OptionalDependencyTests.register(registrar);
		ChapterProgressionTests.register(registrar);
		TeamMergeTests.register(registrar);
		CapabilityTests.register(registrar);
	}
}
