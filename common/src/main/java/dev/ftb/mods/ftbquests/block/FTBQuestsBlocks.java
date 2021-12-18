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

	public static void register() {
		BLOCKS.register();
	}
}
