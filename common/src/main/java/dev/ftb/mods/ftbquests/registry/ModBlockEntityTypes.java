package dev.ftb.mods.ftbquests.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.block.LootCrateOpenerBlock;
import dev.ftb.mods.ftbquests.block.TaskScreenBlock;
import dev.ftb.mods.ftbquests.block.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModBlockEntityTypes {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(FTBQuestsAPI.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

	public static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(String id, BlockEntityType.BlockEntitySupplier<T> factory, Collection<RegistrySupplier<Block>> blocks) {
		return BLOCK_ENTITIES.register(id, () -> new BlockEntityType<T>(factory, blocks.stream().map(Supplier::get).collect(Collectors.toSet())));
	}

	public static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(String id, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<Block> block) {
		return BLOCK_ENTITIES.register(id, () -> new BlockEntityType<T>(factory, Set.of(block.get())));
	}

	public static final RegistrySupplier<BlockEntityType<QuestBarrierBlockEntity>> BARRIER
			= register("barrier", QuestBarrierBlockEntity::new, ModBlocks.BARRIER);
	public static final RegistrySupplier<BlockEntityType<StageBarrierBlockEntity>> STAGE_BARRIER
			= register("stage_barrier", StageBarrierBlockEntity::new, ModBlocks.STAGE_BARRIER);
	public static final RegistrySupplier<BlockEntityType<DetectorBlockEntity>> DETECTOR
			= register("detector", DetectorBlockEntity::new, ModBlocks.DETECTOR);

	public static final RegistrySupplier<BlockEntityType<LootCrateOpenerBlockEntity>> LOOT_CRATE_OPENER
			= register("loot_crate_opener", LootCrateOpenerBlock.blockEntityProvider(), ModBlocks.LOOT_CRATE_OPENER);

	public static final RegistrySupplier<BlockEntityType<TaskScreenBlockEntity>> CORE_TASK_SCREEN
			= register("core_task_screen", TaskScreenBlock.blockEntityProvider(),
			Set.of(ModBlocks.TASK_SCREEN_1, ModBlocks.TASK_SCREEN_3, ModBlocks.TASK_SCREEN_5, ModBlocks.TASK_SCREEN_7));
	public static final RegistrySupplier<BlockEntityType<TaskScreenAuxBlockEntity>> AUX_TASK_SCREEN
			= register("aux_task_screen", TaskScreenBlock.blockEntityAuxProvider(), ModBlocks.AUX_SCREEN);

	public static void register() {
		BLOCK_ENTITIES.register();
	}
}
