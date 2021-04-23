package dev.ftb.mods.ftbquests.block;

import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.registry.DeferredRegister;
import me.shedaniel.architectury.registry.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;

/**
 * @author LatvianModder
 */
public class FTBQuestsBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(FTBQuests.MOD_ID, Registry.BLOCK_REGISTRY);

	public static final RegistrySupplier<Block> BANNER = BLOCKS.register("banner", BannerBlock::new);
	public static final RegistrySupplier<Block> BARRIER = BLOCKS.register("barrier", QuestBarrierBlock::new);
	public static final RegistrySupplier<Block> STAGE_BARRIER = BLOCKS.register("stage_barrier", StageBarrierBlock::new);
	public static final RegistrySupplier<Block> DETECTOR = BLOCKS.register("detector", DetectorBlock::new);

	public static void register() {
		BLOCKS.register();
	}
}
