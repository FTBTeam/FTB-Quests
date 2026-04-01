package dev.ftb.mods.ftbquests.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class LockedCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return literal("locked")
                .requires(FTBQuestsCommands::hasEditorPermission)
                .executes(c -> locked(c.getSource(), c.getSource().getPlayerOrException(), null))
                .then(argument("enabled", BoolArgumentType.bool())
                        .executes(c -> locked(c.getSource(), c.getSource().getPlayerOrException(), BoolArgumentType.getBool(c, "enabled")))
                        .then(argument("player", EntityArgument.player())
                                .executes(c -> locked(c.getSource(), EntityArgument.getPlayer(c, "player"), BoolArgumentType.getBool(c, "enabled")))
                        )
                );
    }

    private static int locked(CommandSourceStack source, ServerPlayer player, @Nullable Boolean locked) {
        return ServerQuestFile.getInstance().getTeamData(player).map(data -> {
            boolean newLocked = Objects.requireNonNullElse(locked, !data.isLocked());

            data.setLocked(newLocked);

            if (newLocked) {
                source.sendSuccess(() -> Component.translatable("commands.ftbquests.locked.enabled", player.getDisplayName()), true);
            } else {
                source.sendSuccess(() -> Component.translatable("commands.ftbquests.locked.disabled", player.getDisplayName()), true);
            }

            return Command.SINGLE_SUCCESS;
        }).orElse(0);
    }
}
