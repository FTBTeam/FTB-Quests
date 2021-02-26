package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftbquests.FTBQuests;
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

	public static void register() {
		BLOCKS.register();
	}
}
