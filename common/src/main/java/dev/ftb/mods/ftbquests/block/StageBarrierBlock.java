package dev.ftb.mods.ftbquests.block;

import com.mojang.serialization.MapCodec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ftb.mods.ftbquests.block.entity.QuestBarrierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StageBarrierBlock extends QuestBarrierBlock {
	private static final MapCodec<StageBarrierBlock> CODEC = simpleCodec(StageBarrierBlock::new);

	public StageBarrierBlock(Properties props) {
		super(props);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return stageBlockEntityProvider().create(blockPos, blockState);
	}

	@Override
	protected MapCodec<StageBarrierBlock> codec() {
		return CODEC;
	}

	@ExpectPlatform
	public static BlockEntityType.BlockEntitySupplier<QuestBarrierBlockEntity> stageBlockEntityProvider() {
		throw new AssertionError();
	}
}
