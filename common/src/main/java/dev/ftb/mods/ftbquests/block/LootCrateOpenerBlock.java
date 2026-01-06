package dev.ftb.mods.ftbquests.block;

import com.mojang.serialization.MapCodec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ftb.mods.ftbquests.block.entity.LootCrateOpenerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class LootCrateOpenerBlock extends BaseEntityBlock {
    private static final MapCodec<LootCrateOpenerBlock> CODEC = simpleCodec(LootCrateOpenerBlock::new);

    public LootCrateOpenerBlock(Properties props) {
        super(props);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return blockEntityProvider().create(blockPos, blockState);
    }

    @ExpectPlatform
    public static BlockEntityType.BlockEntitySupplier<LootCrateOpenerBlockEntity> blockEntityProvider() {
        throw new AssertionError();
    }

    @Override
    protected MapCodec<LootCrateOpenerBlock> codec() {
        return null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(blockPos) instanceof LootCrateOpenerBlockEntity opener) {
                player.displayClientMessage(Component.translatable("block.ftbquests.loot_crate_opener.rightclick", opener.getOutputCount()), true);
            }
        }

        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);

        if (level.getBlockEntity(blockPos) instanceof LootCrateOpenerBlockEntity opener && livingEntity instanceof Player) {
            opener.setOwner(livingEntity.getUUID());
        }
    }
}
