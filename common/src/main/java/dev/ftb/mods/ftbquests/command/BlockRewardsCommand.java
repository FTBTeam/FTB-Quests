package dev.ftb.mods.ftbquests.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static net.minecraft.commands.Commands.argument;

public class BlockRewardsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("block_rewards")
                .requires(PermissionsHelper::hasEditorPermission)
                .executes(c -> toggleRewardBlocking(c.getSource(), c.getSource().getPlayerOrException(), null))
                .then(argument("enabled", BoolArgumentType.bool())
                        .executes(c -> toggleRewardBlocking(c.getSource(), c.getSource().getPlayerOrException(), BoolArgumentType.getBool(c, "enabled")))
                        .then(argument("player", EntityArgument.player())
                                .requires(PermissionsHelper::hasEditorPermission)
                                .executes(c -> toggleRewardBlocking(c.getSource(), EntityArgument.getPlayer(c, "player"), BoolArgumentType.getBool(c, "enabled")))
                        )
                );
    }

    private static int toggleRewardBlocking(CommandSourceStack source, ServerPlayer player, @Nullable Boolean doBlocking) {
        return ServerQuestFile.getInstance().getTeamData(player).map(data -> {
            boolean shouldBlock = Objects.requireNonNullElseGet(doBlocking, () -> !data.areRewardsBlocked());
            data.setRewardsBlocked(shouldBlock);

            source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.rewards_blocked", data.getName(), Component.translatable(data.areRewardsBlocked() ? "gui.yes": "gui.no")), false);

            return Command.SINGLE_SUCCESS;
        }).orElse(0);

    }
}
