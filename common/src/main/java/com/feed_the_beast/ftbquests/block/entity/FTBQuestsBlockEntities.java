package com.feed_the_beast.ftbquests.block.entity;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.block.FTBQuestsBlocks;
import me.shedaniel.architectury.registry.DeferredRegister;
import me.shedaniel.architectury.registry.RegistrySupplier;
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

	public static <T extends BlockEntity> RegistrySupplier<BlockEntityType<?>> register(String id, Supplier<T> factory, Supplier<Block> block) {
		return BLOCK_ENTITIES.register(id, () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
	}

	public static final RegistrySupplier<BlockEntityType<?>> BANNER = register("banner", BannerBlockEntity::new, FTBQuestsBlocks.BANNER);
	public static final RegistrySupplier<BlockEntityType<?>> BARRIER = register("barrier", QuestBarrierBlockEntity::new, FTBQuestsBlocks.BARRIER);

	public static void register() {
		BLOCK_ENTITIES.register();
	}
}
