package dev.ftb.mods.ftbquests.registry;

import dev.ftb.mods.ftblibrary.platform.registry.XRegistry;
import dev.ftb.mods.ftblibrary.platform.registry.XRegistryRef;
import dev.ftb.mods.ftbquests.FTBQuestsPlatform;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.block.entity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModBlockEntityTypes {
	public static final XRegistry<BlockEntityType<?>> BLOCK_ENTITIES = XRegistry.create(FTBQuestsAPI.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

	private static <T extends BlockEntity, I extends T> XRegistryRef<BlockEntityType<I>> register(String id, BlockEntityType.BlockEntitySupplier<I> factory, Collection<XRegistryRef<Block>> blocks) {
		return BLOCK_ENTITIES.register(id, () -> new BlockEntityType<>(factory, blocks.stream().map(Supplier::get).collect(Collectors.toSet())));
	}

	private static <T extends BlockEntity, I extends T> XRegistryRef<BlockEntityType<I>> register(String id, BlockEntityType.BlockEntitySupplier<I> factory, Supplier<Block> block) {
		return BLOCK_ENTITIES.register(id, () -> new BlockEntityType<>(factory, Set.of(block.get())));
	}

	public static final XRegistryRef<BlockEntityType<QuestBarrierBlockEntity>> BARRIER
			= register("barrier", QuestBarrierBlockEntity::new, ModBlocks.BARRIER);
	public static final XRegistryRef<BlockEntityType<StageBarrierBlockEntity>> STAGE_BARRIER
			= register("stage_barrier", StageBarrierBlockEntity::new, ModBlocks.STAGE_BARRIER);

	public static final XRegistryRef<BlockEntityType<DetectorBlockEntity>> DETECTOR
			= register("detector", DetectorBlockEntity::new, ModBlocks.DETECTOR);

	public static final XRegistryRef<BlockEntityType<LootCrateOpenerBlockEntity>> LOOT_CRATE_OPENER
			= register("loot_crate_opener", FTBQuestsPlatform.get().lootCrateBlockEntityProvider(), ModBlocks.LOOT_CRATE_OPENER);

	public static final XRegistryRef<BlockEntityType<TaskScreenBlockEntity>> CORE_TASK_SCREEN
			= register("core_task_screen", FTBQuestsPlatform.get().taskScreenBlockEntityProvider(),
			Set.of(ModBlocks.TASK_SCREEN_1, ModBlocks.TASK_SCREEN_3, ModBlocks.TASK_SCREEN_5, ModBlocks.TASK_SCREEN_7));
	public static final XRegistryRef<BlockEntityType<TaskScreenAuxBlockEntity>> AUX_TASK_SCREEN
			= register("aux_task_screen", FTBQuestsPlatform.get().taskScreenAuxBlockEntityProvider(), ModBlocks.AUX_SCREEN);

	public static void register() {
		BLOCK_ENTITIES.init();
	}
}
