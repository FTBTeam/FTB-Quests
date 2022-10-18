package dev.ftb.mods.ftbquests.block.entity;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.block.FTBQuestsBlocks;
import dev.ftb.mods.ftbquests.block.TaskScreenBlock;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class FTBQuestsBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(FTBQuests.MOD_ID, Registry.BLOCK_ENTITY_TYPE_REGISTRY);

	public static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(String id, BlockEntityType.BlockEntitySupplier<T> factory, Collection<RegistrySupplier<Block>> blocks) {
		return BLOCK_ENTITIES.register(id, () -> BlockEntityType.Builder.of(factory, blocks.stream().map(Supplier::get).toArray(Block[]::new)).build(null));
	}

	public static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(String id, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<Block> block) {
		return BLOCK_ENTITIES.register(id, () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
	}

	public static final RegistrySupplier<BlockEntityType<QuestBarrierBlockEntity>> BARRIER
			= register("barrier", QuestBarrierBlockEntity::new, FTBQuestsBlocks.BARRIER);
	public static final RegistrySupplier<BlockEntityType<StageBarrierBlockEntity>> STAGE_BARRIER
			= register("stage_barrier", StageBarrierBlockEntity::new, FTBQuestsBlocks.STAGE_BARRIER);
	public static final RegistrySupplier<BlockEntityType<DetectorBlockEntity>> DETECTOR
			= register("detector", DetectorBlockEntity::new, FTBQuestsBlocks.DETECTOR);

	public static final RegistrySupplier<BlockEntityType<TaskScreenBlockEntity>> CORE_TASK_SCREEN
			= register("core_task_screen", TaskScreenBlock.blockEntityProvider(),
			Set.of(FTBQuestsBlocks.TASK_SCREEN_1, FTBQuestsBlocks.TASK_SCREEN_3, FTBQuestsBlocks.TASK_SCREEN_5, FTBQuestsBlocks.TASK_SCREEN_7));
	public static final RegistrySupplier<BlockEntityType<TaskScreenAuxBlockEntity>> AUX_TASK_SCREEN
			= register("aux_task_screen", TaskScreenBlock.blockEntityAuxProvider(), FTBQuestsBlocks.AUX_SCREEN);

	public static void register() {
		BLOCK_ENTITIES.register();
	}
}
