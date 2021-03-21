package com.feed_the_beast.ftbquests.block;

import com.feed_the_beast.ftbquests.block.entity.QuestBarrierBlockEntity;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import me.shedaniel.architectury.hooks.EntityHooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class QuestBarrierBlock extends BaseEntityBlock {

	public static final BooleanProperty COMPLETED = BooleanProperty.create("completed");

	protected QuestBarrierBlock() {
		super(Properties.of(Material.BARRIER, MaterialColor.COLOR_LIGHT_BLUE)
				.noOcclusion()
				.noDrops()
				.isViewBlocking((a, b, c) -> false)
				.strength(-1, 6000000F));

		registerDefaultState(defaultBlockState().setValue(COMPLETED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(COMPLETED);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		//return state.getValue(COMPLETED) ? RenderShape.INVISIBLE : super.getRenderShape(state);
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter bg, BlockPos pos, CollisionContext ctx) {
		Entity entity = EntityHooks.fromCollision(ctx);
		BlockEntity be = bg.getBlockEntity(pos);
		if (entity instanceof Player && be instanceof QuestBarrierBlockEntity) {
			Player player = (Player) entity;
			QuestBarrierBlockEntity barrier = (QuestBarrierBlockEntity) be;

			if (barrier.isComplete(ServerQuestFile.INSTANCE.getData(player))) {
				return Shapes.empty();
			}
		}
		return super.getCollisionShape(state, bg, pos, ctx);
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext ctx) {
		return Shapes.empty();
	}

	@Environment(EnvType.CLIENT)
	public float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return 1.0F;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter bg, BlockPos pos) {
		return true;
	}

	@Environment(EnvType.CLIENT)
	public boolean skipRendering(BlockState state, BlockState state2, Direction dir) {
		return state2.is(this) || super.skipRendering(state, state2, dir);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
		super.setPlacedBy(level, pos, state, entity, stack);
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof QuestBarrierBlockEntity
				&& stack.hasCustomHoverName()) {
			((QuestBarrierBlockEntity) be).object = ServerQuestFile.INSTANCE.getID(stack.getHoverName().getString());
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
		BlockEntity be = level.getBlockEntity(pos);
		QuestFile file = ServerQuestFile.INSTANCE;
		ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
		if (be instanceof QuestBarrierBlockEntity
				&& file.getData(player).getCanEdit()
				&& stack.getItem() == Items.NAME_TAG
				&& stack.hasCustomHoverName()) {
			((QuestBarrierBlockEntity) be).object = file.getID(stack.getHoverName().getString());
			player.swing(hand);
		}
		return super.use(state, level, pos, player, hand, result);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockGetter bg) {
		return new QuestBarrierBlockEntity();
	}

}
