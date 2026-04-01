package dev.ftb.mods.ftbquests.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbquests.FTBQuestsPlatform;
import dev.ftb.mods.ftbquests.block.entity.ITaskScreen;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenAuxBlockEntity;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.item.ScreenBlockItem;
import dev.ftb.mods.ftbquests.net.BlockConfigRequestMessage;
import dev.ftb.mods.ftbquests.net.BlockConfigRequestMessage.BlockType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class TaskScreenBlock extends BaseEntityBlock {
    private static final MapCodec<TaskScreenBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(propertiesCodec())
                    .and(Codec.INT.fieldOf("size").forGetter(TaskScreenBlock::getSize))
                    .apply(instance, TaskScreenBlock::new)
    );

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public static Properties createProps(ResourceKey<Block> id) {
        return Properties.of().mapColor(DyeColor.BLACK).strength(0.3f).setId(id);
    }

    private final int size;

    public TaskScreenBlock(Properties props, int size) {
        super(props);
        this.size = size;

        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    public int getSize() {
        return size;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return FTBQuestsPlatform.get().taskScreenBlockEntityProvider().create(blockPos, blockState);
    }

    @Override
    protected MapCodec<? extends TaskScreenBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        if (!validatePlaceable(blockPlaceContext)) {
            return null;
        }
        return super.getStateForPlacement(blockPlaceContext).setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    private boolean validatePlaceable(BlockPlaceContext ctx) {
        int screenSize = ScreenBlockItem.getSize(ctx.getItemInHand());

        if (screenSize == 1) return true; // trivial case

        Direction facing = ctx.getHorizontalDirection();
        return BlockPos.betweenClosedStream(getMultiblockBounds(ctx.getClickedPos(), getSize(), facing))
                .allMatch(pos -> ctx.getLevel().getBlockState(pos).canBeReplaced(ctx));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);

        if (level.getBlockEntity(blockPos) instanceof TaskScreenBlockEntity coreScreen) {
            if (livingEntity instanceof ServerPlayer sp) {
                ServerQuestFile.getInstance().getTeamData(sp).ifPresent(d -> coreScreen.setTeamId(d.getTeamId()));
            }

            Direction facing = blockState.getValue(FACING);
            BlockState auxState = ModBlocks.AUX_SCREEN.get().defaultBlockState().setValue(FACING, facing);
            BlockPos.betweenClosedStream(getMultiblockBounds(blockPos, getSize(), facing))
                    .filter(pos -> !pos.equals(blockPos))
                    .forEach(auxPos -> {
                        level.setBlockAndUpdate(auxPos, auxState);
                        if (level.getBlockEntity(auxPos) instanceof TaskScreenAuxBlockEntity auxScreen) {
                            auxScreen.setCoreScreen(coreScreen);
                        }
                    });
        }
    }

    @Override
    public float getDestroyProgress(BlockState blockState, Player player, BlockGetter blockGetter, BlockPos blockPos) {
        if (player.level().getBlockEntity(blockPos) instanceof ITaskScreen taskScreen && taskScreen.isIndestructible()) {
            return 0f;
        }
        return super.getDestroyProgress(blockState, player, blockGetter, blockPos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (player instanceof ServerPlayer sp && level.getBlockEntity(blockPos) instanceof ITaskScreen taskScreen) {
            if (taskScreen.hasPermissionToEdit(sp)) {
                taskScreen.getCoreScreen().ifPresent(coreScreen ->
                        Server2PlayNetworking.send(sp, new BlockConfigRequestMessage(coreScreen.getBlockPos(), BlockType.TASK_SCREEN)));
            } else {
                sp.sendOverlayMessage(Component.translatable("block.ftbquests.screen.no_permission").withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    /**
     * Get the overall bounds of the multiblock, given the core screen pos
     * @param corePos bottom middle block of the screen
     * @param size the multiblock size (1,3,5 or 7)
     * @param facing the side the display screen is on
     * @return the bounding box containing all blocks of the multiblock
     */
    public static BoundingBox getMultiblockBounds(BlockPos corePos, int size, Direction facing) {
        if (size == 1) return new BoundingBox(corePos);

        int size2 = size / 2;
        facing = facing.getCounterClockWise();

        BlockPos pos1 = new BlockPos(corePos.getX() - size2 * facing.getStepX(), corePos.getY(), corePos.getZ() - size2 * facing.getStepZ());
        BlockPos pos2 = new BlockPos(corePos.getX() + size2 * facing.getStepX(), corePos.getY() + size - 1, corePos.getZ() + size2 * facing.getStepZ());
        return BoundingBox.fromCorners(pos1, pos2);
    }

    public static class Aux extends TaskScreenBlock {
        private static final MapCodec<Aux> CODEC = simpleCodec(Aux::new);

        public Aux(Properties props) {
            super(props, 0);
        }

        @Override
        public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
            return FTBQuestsPlatform.get().taskScreenAuxBlockEntityProvider().create(blockPos, blockState);
        }

        @Override
        protected MapCodec<Aux> codec() {
            return CODEC;
        }
    }
}
