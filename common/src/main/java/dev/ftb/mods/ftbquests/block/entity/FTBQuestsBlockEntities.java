package dev.ftb.mods.ftbquests.block.entity;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.block.FTBQuestsBlocks;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class FTBQuestsBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(FTBQuests.MOD_ID, Registry.BLOCK_ENTITY_TYPE_REGISTRY);

	public static <T extends BlockEntity> RegistrySupplier<BlockEntityType<?>> register(String id, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<Block> block) {
		return BLOCK_ENTITIES.register(id, () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
	}

	public static final RegistrySupplier<BlockEntityType<?>> BARRIER = register("barrier", QuestBarrierBlockEntity::new, FTBQuestsBlocks.BARRIER);
	public static final RegistrySupplier<BlockEntityType<?>> STAGE_BARRIER = register("stage_barrier", StageBarrierBlockEntity::new, FTBQuestsBlocks.STAGE_BARRIER);
	public static final RegistrySupplier<BlockEntityType<?>> DETECTOR = register("detector", DetectorBlockEntity::new, FTBQuestsBlocks.DETECTOR);

	public static void register() {
		BLOCK_ENTITIES.register();
	}
}
