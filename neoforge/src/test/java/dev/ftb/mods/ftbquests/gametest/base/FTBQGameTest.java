package dev.ftb.mods.ftbquests.gametest.base;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public final class FTBQGameTest extends GameTestInstance {

	public static final MapCodec<FTBQGameTest> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			TestData.CODEC.forGetter(test -> test.info()),
			Codec.STRING.fieldOf("testname").forGetter(FTBQGameTest::testName)
	).apply(builder, FTBQGameTest::new));

	private final String testName;

	public FTBQGameTest(TestData<Holder<TestEnvironmentDefinition<?>>> info, String testName) {
		super(info);
		this.testName = testName;
	}

	@Override
	public void run(GameTestHelper helper) {
		Consumer<GameTestHelper> consumer = FTBQTests.get(testName);
		if (consumer == null) {
			throw new GameTestAssertException(Component.literal("unknown test " + testName), 0);
		}
		consumer.accept(helper);
	}

	public String testName() {
		return testName;
	}

	@Override
	public MapCodec<? extends GameTestInstance> codec() {
		return CODEC;
	}

	@Override
	protected MutableComponent typeDescription() {
		return Component.literal("ftbquests:inline");
	}
}
