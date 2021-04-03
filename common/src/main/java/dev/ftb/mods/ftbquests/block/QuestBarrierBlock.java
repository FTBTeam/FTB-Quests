package dev.ftb.mods.ftbquests.block;

import dev.ftb.mods.ftbquests.block.entity.BarrierBlockEntity;
import dev.ftb.mods.ftbquests.block.entity.FTBQuestsBlockEntities;
import me.shedaniel.architectury.hooks.EntityHooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class QuestBarrierBlock extends BaseEntityBlock {
	public static final BooleanProperty OPEN = BooleanProperty.create("open");

	protected QuestBarrierBlock() {
		super(Properties.of(Material.BARRIER, MaterialColor.COLOR_LIGHT_BLUE)
				.noOcclusion()
				.noDrops()
				.isViewBlocking((a, b, c) -> false)
				.strength(-1, 6000000F)
				.lightLevel(blockState -> 3)
				.emissiveRendering((blockState, blockGetter, blockPos) -> true)
		);

		registerDefaultState(defaultBlockState().setValue(OPEN, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(OPEN);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter bg, BlockPos pos, CollisionContext ctx) {
		Entity entity = EntityHooks.fromCollision(ctx);

		if (entity instanceof Player) {
			BlockEntity be = bg.getBlockEntity(pos);

			if (be instanceof BarrierBlockEntity && ((BarrierBlockEntity) be).isOpen((Player) entity)) {
				return Shapes.empty();
			}
		}

		return super.getCollisionShape(state, bg, pos, ctx);
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext ctx) {
		return Shapes.empty();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return 1.0F;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter bg, BlockPos pos) {
		return true;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean skipRendering(BlockState state, BlockState state2, Direction dir) {
		return state2.is(this) || super.skipRendering(state, state2, dir);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
		super.setPlacedBy(level, pos, state, entity, stack);

		if (!level.isClientSide() && stack.hasCustomHoverName()) {
			BlockEntity blockEntity = level.getBlockEntity(pos);

			if (blockEntity instanceof BarrierBlockEntity) {
				((BarrierBlockEntity) blockEntity).update(stack.getHoverName().getString());
			}
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockGetter bg) {
		return FTBQuestsBlockEntities.createQuestBarrierEntity();
	}
}
