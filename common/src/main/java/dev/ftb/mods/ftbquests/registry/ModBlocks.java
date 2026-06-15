package dev.ftb.mods.ftbquests.registry;

import dev.ftb.mods.ftblibrary.platform.registry.XRegistry;
import dev.ftb.mods.ftblibrary.platform.registry.XRegistryRef;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.block.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;

public class ModBlocks {
	public static final XRegistry<Block> BLOCKS = XRegistry.create(FTBQuestsAPI.MOD_ID, Registries.BLOCK);

	public static final XRegistryRef<Block> BARRIER
			= register("barrier", (id) -> new QuestBarrierBlock(QuestBarrierBlock.createProps(id)));
	public static final XRegistryRef<Block> STAGE_BARRIER
			= register("stage_barrier", (id) -> new StageBarrierBlock(QuestBarrierBlock.createProps(id)));
	public static final XRegistryRef<Block> DETECTOR
			= register("detector", (id) -> new DetectorBlock(BlockBehaviour.Properties.of().strength(0.3F).setId(id)));
	public static final XRegistryRef<Block> LOOT_CRATE_OPENER
			= register("loot_crate_opener", (id) -> new LootCrateOpenerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.8f).setId(id)));

	public static final XRegistryRef<Block> TASK_SCREEN_1
			= register("screen_1", (id) -> new TaskScreenBlock(TaskScreenBlock.createProps(id), 1));
	public static final XRegistryRef<Block> TASK_SCREEN_3
			= register("screen_3", (id) -> new TaskScreenBlock(TaskScreenBlock.createProps(id), 3));
	public static final XRegistryRef<Block> TASK_SCREEN_5
			= register("screen_5", (id) -> new TaskScreenBlock(TaskScreenBlock.createProps(id), 5));
	public static final XRegistryRef<Block> TASK_SCREEN_7
			= register("screen_7", (id) -> new TaskScreenBlock(TaskScreenBlock.createProps(id), 7));
	public static final XRegistryRef<Block> AUX_SCREEN
			= register("aux_task_screen", (id) -> new TaskScreenBlock.Aux(TaskScreenBlock.createProps(id)));

	public static void register() {
		BLOCKS.init();
	}

	private static XRegistryRef<Block> register(String name, Function<ResourceKey<Block>, Block> block) {
		var id = ResourceKey.create(Registries.BLOCK, FTBQuestsAPI.id(name));
		return BLOCKS.register(name, () -> block.apply(id));
	}
}
