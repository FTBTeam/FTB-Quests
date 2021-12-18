package dev.ftb.mods.ftbquests.block.entity;

import dev.ftb.mods.ftbquests.FTBQuests;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static dev.ftb.mods.ftbquests.block.QuestBarrierBlock.OPEN;

public interface BarrierBlockEntity {
	void update(String s);

	boolean isOpen(Player player);

	// will be set by extending BlockEntity
	void setChanged();

	static void tick(Level level, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
		if (blockEntity instanceof BarrierBlockEntity barrier) {
			if (level.isClientSide && FTBQuests.PROXY.isClientDataLoaded() && level.getGameTime() % 5L == 0L) {
				boolean completed = barrier.isOpen(FTBQuests.PROXY.getClientPlayer());

				if (completed != blockState.getValue(OPEN)) {
					level.setBlock(blockPos, blockState.setValue(OPEN, completed), 2 | 8);
					barrier.setChanged();
				}
			}
		}
	}
}
