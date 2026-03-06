package dev.ftb.mods.ftbquests.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

import static net.minecraft.commands.Commands.argument;

public class BlockRewardsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("block_rewards")
                .requires(FTBQuestsCommands::hasEditorPermission)
                .executes(c -> toggleRewardBlocking(c.getSource(), c.getSource().getPlayerOrException(), null))
                .then(argument("enabled", BoolArgumentType.bool())
                        .executes(c -> toggleRewardBlocking(c.getSource(), c.getSource().getPlayerOrException(), BoolArgumentType.getBool(c, "enabled")))
                        .then(argument("player", EntityArgument.player())
                                .requires(FTBQuestsCommands::hasEditorPermission)
                                .executes(c -> toggleRewardBlocking(c.getSource(), EntityArgument.getPlayer(c, "player"), BoolArgumentType.getBool(c, "enabled")))
                        )
                );
    }

    private static int toggleRewardBlocking(CommandSourceStack source, ServerPlayer player, @Nullable Boolean doBlocking) {
        return ServerQuestFile.getInstance().getTeamData(player).map(data -> {
            boolean shouldBlock = Objects.requireNonNullElse(doBlocking, !data.areRewardsBlocked());
            data.setRewardsBlocked(shouldBlock);

            source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.rewards_blocked", data, data.areRewardsBlocked()), false);

            return Command.SINGLE_SUCCESS;
        }).orElse(0);

    }
}
