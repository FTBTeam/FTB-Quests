package dev.ftb.mods.ftbquests.gametest;

import com.mojang.serialization.DataResult;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.gametest.base.FTBQTestRegistrar;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

import static dev.ftb.mods.ftbquests.gametest.base.QuestTestSupport.assertTrue;

public final class SerializationTests {

	private SerializationTests() {}

	public static void register(FTBQTestRegistrar registrar) {
		registrar.add("serialization/test_instances_are_serializable", 20, helper -> helper.runAfterDelay(1, () -> {
			RegistryAccess access = helper.getLevel().registryAccess();
			Registry<GameTestInstance> registry = access.lookupOrThrow(Registries.TEST_INSTANCE);
			var ops = access.createSerializationContext(NbtOps.INSTANCE);

			for (var entry : registry.entrySet()) {
				Identifier id = entry.getKey().identifier();
				if (id.getNamespace().equals(FTBQuestsAPI.MOD_ID)) {
					DataResult<Tag> result = GameTestInstance.DIRECT_CODEC.encodeStart(ops, entry.getValue());
					assertTrue(helper, result.isSuccess(), "test instance " + id + " must encode for registry sync: " + result.error().map(DataResult.Error::message).orElse(""));
				}
			}

			helper.succeed();
		}));
	}
}
