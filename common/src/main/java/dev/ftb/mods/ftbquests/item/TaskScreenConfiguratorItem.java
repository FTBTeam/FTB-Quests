package dev.ftb.mods.ftbquests.item;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.block.entity.ITaskScreen;
import dev.ftb.mods.ftbquests.net.BlockConfigRequestMessage;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class TaskScreenConfiguratorItem extends Item {
    public TaskScreenConfiguratorItem() {
        super(ModItems.defaultProps());
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (ctx.getPlayer() instanceof ServerPlayer sp) {
            if (level.getBlockEntity(ctx.getClickedPos()) instanceof ITaskScreen) {
                storeBlockPos(ctx.getItemInHand(), ctx.getLevel(), ctx.getClickedPos());
                ctx.getPlayer().level().playSound(null, ctx.getClickedPos(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 1f, 1f);
                ctx.getPlayer().displayClientMessage(Component.translatable("ftbquests.message.configurator_bound", posToString(ctx.getClickedPos())), true);
            } else {
                return tryUseOn(sp, ctx.getItemInHand()) ? InteractionResult.CONSUME : InteractionResult.FAIL;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer sp) {
            return tryUseOn(sp, stack) ? InteractionResult.CONSUME : InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        if (context.registries() != null) {
            consumer.accept(Component.translatable("item.ftbquests.task_screen_configurator.tooltip").withStyle(ChatFormatting.GRAY));

            readBlockPos(itemStack).ifPresent(gPos -> {
                String str = gPos.dimension().identifier() + " / " + posToString(gPos.pos());
                consumer.accept(Component.translatable("ftbquests.message.configurator_bound", str).withStyle(ChatFormatting.DARK_AQUA));
            });
        }
    }

    private boolean tryUseOn(ServerPlayer player, ItemStack stack) {
        return readBlockPos(stack).map(gPos -> {
            Level level = player.level().getServer().getLevel(gPos.dimension());
            if (level != player.level() || !player.level().isLoaded(gPos.pos())) {
                player.displayClientMessage(Component.translatable("ftbquests.message.task_screen_inaccessible").withStyle(ChatFormatting.RED), true);
                return false;
            }
            if (level.getBlockEntity(gPos.pos()) instanceof ITaskScreen taskScreen) {
                if (taskScreen.hasPermissionToEdit(player)) {
                    taskScreen.getCoreScreen().ifPresent(coreScreen ->
                            NetworkManager.sendToPlayer(player, new BlockConfigRequestMessage(coreScreen.getBlockPos(), BlockConfigRequestMessage.BlockType.TASK_SCREEN)));
                    return true;
                } else {
                    player.displayClientMessage(Component.translatable("block.ftbquests.screen.no_permission").withStyle(ChatFormatting.RED), true);
                }
            } else {
                player.displayClientMessage(Component.translatable("ftbquests.message.missing_task_screen").withStyle(ChatFormatting.RED), true);
            }
            return false;
        }).orElse(false);
    }

    public static void storeBlockPos(ItemStack stack, Level level, BlockPos clickedPos) {
        stack.set(ModDataComponents.SCREEN_POS.get(), GlobalPos.of(level.dimension(), clickedPos));
    }

    public static Optional<GlobalPos> readBlockPos(ItemStack stack) {
        return FTBQuests.getComponent(stack, ModDataComponents.SCREEN_POS);
    }

    private static String posToString(BlockPos pos) {
        return String.format("[%d,%d,%d]", pos.getX(), pos.getY(), pos.getZ());
    }
}
