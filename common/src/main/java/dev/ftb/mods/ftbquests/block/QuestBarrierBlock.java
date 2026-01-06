package dev.ftb.mods.ftbquests.block;

import com.mojang.serialization.MapCodec;
import dev.architectury.hooks.level.entity.EntityHooks;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.block.entity.BaseBarrierBlockEntity;
import dev.ftb.mods.ftbquests.block.entity.QuestBarrierBlockEntity;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.net.BlockConfigRequestMessage;
import dev.ftb.mods.ftbquests.net.BlockConfigRequestMessage.BlockType;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestBarrierBlock extends BaseEntityBlock {
	private static final MapCodec<QuestBarrierBlock> CODEC = simpleCodec(QuestBarrierBlock::new);

	public static final BooleanProperty OPEN = BooleanProperty.create("open");

	public static Properties createProps(ResourceKey<Block> key) {
		return Properties.of()
				.setId(key)
				.mapColor(MapColor.COLOR_LIGHT_BLUE)
				.pushReaction(PushReaction.BLOCK)
				.noOcclusion()
				.isViewBlocking((blockState, blockGetter, blockPos) -> false)
				.isSuffocating((blockState, blockGetter, blockPos) -> false)
				.strength(-1, 6000000F)
				.lightLevel(blockState -> 3)
				.emissiveRendering((blockState, blockGetter, blockPos) -> true);
	}

	public QuestBarrierBlock(Properties props) {
		super(props);

		registerDefaultState(defaultBlockState().setValue(OPEN, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(OPEN);
	}

	@Override
	protected MapCodec<? extends QuestBarrierBlock> codec() {
		return CODEC;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter bg, BlockPos pos, CollisionContext ctx) {
		if (EntityHooks.fromCollision(ctx) instanceof Player player
				&& bg.getBlockEntity(pos) instanceof BaseBarrierBlockEntity barrier
				&& barrier.isOpen(player)) {
			return Shapes.empty();
		}

		return super.getCollisionShape(state, bg, pos, ctx);
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter bg, BlockPos pos, CollisionContext ctx) {
		if (EntityHooks.fromCollision(ctx) instanceof Player player && blockState.getValue(OPEN) && !NetUtils.canEdit(player)) {
			return Shapes.empty();
		}

		return super.getShape(blockState, bg, pos, ctx);
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
    protected boolean propagatesSkylightDown(BlockState blockState) {
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

		if (!level.isClientSide()
				&& stack.has(DataComponents.CUSTOM_NAME)
				&& !stack.has(ModDataComponents.BARRIER_SAVED.get())
				&& level.getBlockEntity(pos) instanceof BaseBarrierBlockEntity barrier)
		{
			barrier.updateFromString(stack.getHoverName().getString());
		}
	}

	@Override
	protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
		if (player instanceof ServerPlayer sp) {
			if (level.getBlockEntity(blockPos) instanceof BaseBarrierBlockEntity barrier && barrier.hasPermissionToEdit(sp)) {
				NetworkManager.sendToPlayer(sp, new BlockConfigRequestMessage(blockPos, BlockType.BARRIER));
                return InteractionResult.SUCCESS;
//				return ItemInteractionResult.sidedSuccess(level.isClientSide);
			}
			return InteractionResult.FAIL;
		} else {
			return InteractionResult.SUCCESS;
		}
	}

    @Override
    protected ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean bl) {
        ItemStack stack = super.getCloneItemStack(levelReader, blockPos, blockState, bl);
        if (levelReader.getBlockEntity(blockPos) instanceof BaseBarrierBlockEntity barrier) {
            stack.set(ModDataComponents.BARRIER_SAVED.get(), BaseBarrierBlockEntity.BarrierSavedData.fromBlockEntity(barrier));
            if (!barrier.getSkin().isEmpty() && !ClientQuestFile.canClientPlayerEdit()) {
                stack.set(DataComponents.ITEM_NAME, barrier.getSkin().getHoverName());
            }
        }
        return stack;
    }

    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier, boolean bl) {
        if (entity instanceof ServerPlayer player && player.level().getServer() != null && level.getBlockEntity(blockPos) instanceof BaseBarrierBlockEntity b) {
            b.optionalTeleportData().ifPresent(teleportData -> {
                if (teleportData.enabled() && b.isOpen(player)) {
                    TeleportTicker.addPending(player, teleportData.effectiveDest(b.getBlockPos()));
                }
            });
        }
    }

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return level != null && level.isClientSide() ? BaseBarrierBlockEntity::tick : null;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return questBlockEntityProvider().create(blockPos, blockState);
	}

	@ExpectPlatform
	public static BlockEntityType.BlockEntitySupplier<QuestBarrierBlockEntity> questBlockEntityProvider() {
		throw new AssertionError();
	}

	public static class TeleportTicker {
		private static final Map<UUID, BaseBarrierBlockEntity.TeleportData> pendingTeleports = new HashMap<>();

		public static void tick(MinecraftServer server) {
			if (!pendingTeleports.isEmpty()) {
				pendingTeleports.forEach((id, teleportData) ->
						teleportData.teleportPlayer(server.getPlayerList().getPlayer(id))
				);
				pendingTeleports.clear();
			}
		}

		public static void addPending(ServerPlayer player, BaseBarrierBlockEntity.TeleportData teleportData) {
			pendingTeleports.put(player.getUUID(), teleportData);
		}
	}
}
