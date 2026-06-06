package dev.ftb.mods.ftbquests.gametest.base;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.List;
import java.util.function.Consumer;

public final class FTBQTestRegistrar {

	private static final Identifier STRUCTURE = FTBQuestsAPI.id("empty_5x5x7");

	private final RegisterGameTestsEvent event;
	private final Holder<TestEnvironmentDefinition<?>> environment;

	public FTBQTestRegistrar(RegisterGameTestsEvent event) {
		this.event = event;
		this.environment = event.registerEnvironment(FTBQuestsAPI.id("default"), new TestEnvironmentDefinition.AllOf(List.of()));
	}

	public void add(String name, int maxTicks, Consumer<GameTestHelper> body) {
		add(name, maxTicks, 0, body);
	}

	public void add(String name, int maxTicks, int setupTicks, Consumer<GameTestHelper> body) {
		TestData<Holder<TestEnvironmentDefinition<?>>> info = new TestData<>(
				environment,
				STRUCTURE,
				maxTicks,
				setupTicks,
				true,
				Rotation.NONE
		);
		try {
			event.registerTest(FTBQuestsAPI.id(name), new FTBQInlineTest(info, body));
		} catch (Throwable t) {
			System.err.println("[FTBQGameTest] Failed to register " + name + ": " + t);
			t.printStackTrace();
		}
	}
}
