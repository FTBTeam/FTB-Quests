package dev.ftb.mods.ftbquests.gametest.base;

import net.minecraft.gametest.framework.GameTestHelper;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class FTBQTests {

	private static final Map<String, Consumer<GameTestHelper>> BODIES = new HashMap<>();

	private FTBQTests() {}

	public static void put(String name, Consumer<GameTestHelper> body) {
		BODIES.put(name, body);
	}

	@Nullable
	public static Consumer<GameTestHelper> get(String name) {
		return BODIES.get(name);
	}
}
