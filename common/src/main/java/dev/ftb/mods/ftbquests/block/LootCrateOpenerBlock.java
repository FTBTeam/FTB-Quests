package dev.ftb.mods.ftbquests.block;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ftb.mods.ftbquests.block.entity.LootCrateOpenerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class LootCrateOpenerBlock extends Block implements EntityBlock {
    public LootCrateOpenerBlock() {
        super(Properties.of(Material.WOOD, MaterialColor.WOOD).strength(1.8f));
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
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(blockPos) instanceof LootCrateOpenerBlockEntity opener) {
                player.displayClientMessage(new TranslatableComponent("block.ftbquests.loot_crate_opener.rightclick", opener.getOutputCount()), true);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);

        if (level.getBlockEntity(blockPos) instanceof LootCrateOpenerBlockEntity opener && livingEntity instanceof Player) {
            opener.setOwner(livingEntity.getUUID());
        }
    }

}
