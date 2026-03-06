package dev.ftb.mods.ftbquests.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.block.DetectorBlock;
import dev.ftb.mods.ftbquests.block.LootCrateOpenerBlock;
import dev.ftb.mods.ftbquests.block.QuestBarrierBlock;
import dev.ftb.mods.ftbquests.block.StageBarrierBlock;
import dev.ftb.mods.ftbquests.block.TaskScreenBlock;

import java.util.function.Function;

public class ModBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(FTBQuestsAPI.MOD_ID, Registries.BLOCK);

	public static final RegistrySupplier<Block> BARRIER
			= register("barrier", (id) -> new QuestBarrierBlock(QuestBarrierBlock.createProps(id)));
	public static final RegistrySupplier<Block> STAGE_BARRIER
			= register("stage_barrier", (id) -> new StageBarrierBlock(QuestBarrierBlock.createProps(id)));
	public static final RegistrySupplier<Block> DETECTOR
			= register("detector", (id) -> new DetectorBlock(BlockBehaviour.Properties.of().strength(0.3F).setId(id)));
	public static final RegistrySupplier<Block> LOOT_CRATE_OPENER
			= register("loot_crate_opener", (id) -> new LootCrateOpenerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.8f).setId(id)));

	public static final RegistrySupplier<Block> TASK_SCREEN_1
			= register("screen_1", (id) -> new TaskScreenBlock(TaskScreenBlock.createProps(id), 1));
	public static final RegistrySupplier<Block> TASK_SCREEN_3
			= register("screen_3", (id) -> new TaskScreenBlock(TaskScreenBlock.createProps(id), 3));
	public static final RegistrySupplier<Block> TASK_SCREEN_5
			= register("screen_5", (id) -> new TaskScreenBlock(TaskScreenBlock.createProps(id), 5));
	public static final RegistrySupplier<Block> TASK_SCREEN_7
			= register("screen_7", (id) -> new TaskScreenBlock(TaskScreenBlock.createProps(id), 7));
	public static final RegistrySupplier<Block> AUX_SCREEN
			= register("aux_task_screen", (id) -> new TaskScreenBlock.Aux(TaskScreenBlock.createProps(id)));

	public static void register() {
		BLOCKS.register();
	}

	private static RegistrySupplier<Block> register(String name, Function<ResourceKey<Block>, Block> block) {
		var id = ResourceKey.create(Registries.BLOCK, FTBQuestsAPI.id(name));
		return BLOCKS.register(name, () -> block.apply(id));
	}
}
