package dev.ftb.mods.ftbquests.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.ftb.mods.ftbquests.FTBQuests;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;

/**
 * @author LatvianModder
 */
public class FTBQuestsBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(FTBQuests.MOD_ID, Registry.BLOCK_REGISTRY);

	public static final RegistrySupplier<Block> BARRIER = BLOCKS.register("barrier", QuestBarrierBlock::new);
	public static final RegistrySupplier<Block> STAGE_BARRIER = BLOCKS.register("stage_barrier", StageBarrierBlock::new);
	public static final RegistrySupplier<Block> DETECTOR = BLOCKS.register("detector", DetectorBlock::new);
	public static final RegistrySupplier<Block> LOOT_CRATE_OPENER = BLOCKS.register("loot_crate_opener", LootCrateOpenerBlock::new);

	public static final RegistrySupplier<Block> TASK_SCREEN_1 = BLOCKS.register("screen_1", () -> new TaskScreenBlock(1));
	public static final RegistrySupplier<Block> TASK_SCREEN_3 = BLOCKS.register("screen_3", () -> new TaskScreenBlock(3));
	public static final RegistrySupplier<Block> TASK_SCREEN_5 = BLOCKS.register("screen_5", () -> new TaskScreenBlock(5));
	public static final RegistrySupplier<Block> TASK_SCREEN_7 = BLOCKS.register("screen_7", () -> new TaskScreenBlock(7));
	public static final RegistrySupplier<Block> AUX_SCREEN = BLOCKS.register("aux_task_screen", TaskScreenBlock.Aux::new);

	public static void register() {
		BLOCKS.register();
	}

}
