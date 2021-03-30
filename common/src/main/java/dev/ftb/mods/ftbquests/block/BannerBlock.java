package dev.ftb.mods.ftbquests.block;

import dev.ftb.mods.ftbquests.block.entity.BannerBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

/**
 * @author LatvianModder
 */
public class BannerBlock extends BaseEntityBlock {
	public BannerBlock() {
		super(BlockBehaviour.Properties.of(Material.BARRIER));
	}

	@Override
	public BlockEntity newBlockEntity(BlockGetter world) {
		return new BannerBlockEntity();
	}
}
