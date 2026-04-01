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

public class EditModeCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return literal("editing_mode")
                .requires(FTBQuestsCommands::isSSPOrEditor)
                .executes(c -> editingMode(c.getSource(), c.getSource().getPlayerOrException(), null))
                .then(argument("enabled", BoolArgumentType.bool())
                        .executes(c -> editingMode(c.getSource(), c.getSource().getPlayerOrException(), BoolArgumentType.getBool(c, "enabled")))
                        .then(argument("player", EntityArgument.player())
                                .executes(c -> editingMode(c.getSource(), EntityArgument.getPlayer(c, "player"), BoolArgumentType.getBool(c, "enabled")))
                        )
                );
    }

    private static int editingMode(CommandSourceStack source, ServerPlayer player, @Nullable Boolean canEdit) {
        return ServerQuestFile.getInstance().getTeamData(player).map(data -> {
            boolean newCanEdit = Objects.requireNonNullElse(canEdit, !data.getCanEdit(player));

            data.setCanEdit(player, newCanEdit);

            if (newCanEdit) {
                source.sendSuccess(() -> Component.translatable("commands.ftbquests.editing_mode.enabled", player.getDisplayName()), true);
            } else {
                source.sendSuccess(() -> Component.translatable("commands.ftbquests.editing_mode.disabled", player.getDisplayName()), true);
            }

            return Command.SINGLE_SUCCESS;
        }).orElse(0);
    }

}
