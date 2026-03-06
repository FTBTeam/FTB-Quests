package dev.ftb.mods.ftbquests.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.util.InventoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

import static net.minecraft.commands.Commands.argument;

public class ExportRewardTableCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("export_reward_table_to_chest")
                .requires(FTBQuestsCommands::hasEditorPermission)
                .then(argument("reward_table", StringArgumentType.string())
                        .executes(ctx ->
                                exportRewards(ctx.getSource(), StringArgumentType.getString(ctx, "reward_table"), null)
                        )
                        .then(argument("pos", BlockPosArgument.blockPos())
                                .executes(ctx -> {
                                    BlockPos pos = BlockPosArgument.getSpawnablePos(ctx, "pos");
                                    return exportRewards(ctx.getSource(), StringArgumentType.getString(ctx, "reward_table"), pos);
                                })
                        )
                );
    }

    private static int exportRewards(CommandSourceStack source, String idStr, @Nullable BlockPos pos) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = source.getLevel();

        if (!(FTBQuestsCommands.getQuestObjectForString(idStr) instanceof RewardTable table)) {
            throw FTBQuestsCommands.NO_OBJECT.create(idStr);
        }

        pos = Objects.requireNonNullElse(pos, BlockPos.containing(player.pick(10, 1F, false).getLocation()));
        if (level.getBlockEntity(pos) == null) {
            throw FTBQuestsCommands.NO_INVENTORY.create();
        }

        List<ItemStack> items = new ArrayList<>();
        for (WeightedReward wr : table.getWeightedRewards()) {
            if (wr.getReward() instanceof ItemReward itemReward) {
                items.add(itemReward.getItem());
            }
        }
        InventoryUtil.putItemsInInventory(items, level, pos, Direction.UP, true);

        source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.table_exported", table.getTitle(), items.size()), false);

        return Command.SINGLE_SUCCESS;
    }
}
