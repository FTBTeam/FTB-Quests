package dev.ftb.mods.ftbquests.block;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ftb.mods.ftbquests.block.entity.ITaskScreen;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenAuxBlockEntity;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.item.ScreenBlockItem;
import dev.ftb.mods.ftbquests.net.TaskScreenConfigRequest;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class TaskScreenBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private final int size;

    protected TaskScreenBlock(int size) {
        super(Properties.of().mapColor(DyeColor.BLACK).strength(0.3f));
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
        return blockEntityProvider().create(blockPos, blockState);
    }

    @ExpectPlatform
    public static BlockEntityType.BlockEntitySupplier<TaskScreenBlockEntity> blockEntityProvider() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BlockEntityType.BlockEntitySupplier<TaskScreenAuxBlockEntity> blockEntityAuxProvider() {
        throw new AssertionError();
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
                coreScreen.setTeamId(ServerQuestFile.INSTANCE.getData(sp).getTeamId());
            }

            Direction facing = blockState.getValue(FACING);
            BlockState auxState = FTBQuestsBlocks.AUX_SCREEN.get().defaultBlockState().setValue(FACING, facing);
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
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (blockState.getBlock() != newState.getBlock() && level.getBlockEntity(blockPos) instanceof ITaskScreen taskScreen) {
            taskScreen.getCoreScreen().ifPresent(coreScreen -> {
                coreScreen.removeAllAuxScreens();
                if (coreScreen != taskScreen) {
                    // we're breaking an auxiliary screen; also need to break the core screen, and drop a block
                    level.destroyBlock(coreScreen.getBlockPos(), true, null);
                }
            });

            super.onRemove(blockState, level, blockPos, newState, isMoving);
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
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (player instanceof ServerPlayer sp && level.getBlockEntity(blockPos) instanceof ITaskScreen taskScreen) {
            if (hasPermissionToEdit(sp, taskScreen)) {
                taskScreen.getCoreScreen().ifPresent(coreScreen -> new TaskScreenConfigRequest(coreScreen.getBlockPos()).sendTo(sp));
            } else {
                sp.displayClientMessage(Component.translatable("block.ftbquests.screen.no_permission").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockGetter, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, blockGetter, list, tooltipFlag);

        if (itemStack.getTag() != null && itemStack.getTag().contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
            CompoundTag subTag = Objects.requireNonNull(itemStack.getTagElement("BlockEntityTag"));
            QuestFile questFile = FTBQuestsClient.getClientQuestFile();
            if (questFile != null) {
                Task task = questFile.getTask(subTag.getLong("TaskID"));
                if (task != null) {
                    list.add(Component.translatable("ftbquests.chapter").append(": ")
                            .append(task.getQuest().getChapter().getTitle().copy().withStyle(ChatFormatting.YELLOW)));
                    list.add(Component.translatable("ftbquests.quest").append(": ").append(task.getQuest().getMutableTitle().withStyle(ChatFormatting.YELLOW)));
                    list.add(Component.translatable("ftbquests.task").append(": ").append(task.getMutableTitle().withStyle(ChatFormatting.YELLOW)));
                }
            }
        }
    }

    public static boolean hasPermissionToEdit(ServerPlayer player, ITaskScreen screen) {
        // either the player must be the owner of the screen...
        if (player.getUUID().equals(screen.getTeamId())) {
            return true;
        }

        // ...or in the same team as the owner of the screen
        return FTBTeamsAPI.api().getManager().getTeamByID(screen.getTeamId())
                .map(team -> team.getRankForPlayer(player.getUUID()).isMemberOrBetter())
                .orElse(false);
    }

    /**
     * Get the overall bounds of the multiblock, given the core screen pos
     * @param corePos bottom middle block of the screen
     * @param size the multiblock size (1,3,5 or 7)
     * @param facing the side the display screen is on
     * @return the bounding box containing all blocks of the multiblock
     */
    public static AABB getMultiblockBounds(BlockPos corePos, int size, Direction facing) {
        if (size == 1) return new AABB(corePos, corePos);

        int size2 = size / 2;
        facing = facing.getCounterClockWise();

        return new AABB(
                corePos.getX() - size2 * facing.getStepX(), corePos.getY(), corePos.getZ() - size2 * facing.getStepZ(),
                corePos.getX() + size2 * facing.getStepX(), corePos.getY() + size - 1, corePos.getZ() + size2 * facing.getStepZ()
        );
    }

    public static class Aux extends TaskScreenBlock {
        protected Aux() {
            super(0);
        }

        @Override
        public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
            return blockEntityAuxProvider().create(blockPos, blockState);
        }

    }
}
