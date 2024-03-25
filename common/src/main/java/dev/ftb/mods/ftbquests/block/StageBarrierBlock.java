package dev.ftb.mods.ftbquests.block;

import com.mojang.serialization.MapCodec;
import dev.ftb.mods.ftbquests.block.entity.StageBarrierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StageBarrierBlock extends QuestBarrierBlock {
	private static final MapCodec<StageBarrierBlock> CODEC = simpleCodec(StageBarrierBlock::new);

	protected StageBarrierBlock(Properties props) {
		super(props);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new StageBarrierBlockEntity(blockPos, blockState);
	}

	@Override
	protected MapCodec<StageBarrierBlock> codec() {
		return CODEC;
	}
}
