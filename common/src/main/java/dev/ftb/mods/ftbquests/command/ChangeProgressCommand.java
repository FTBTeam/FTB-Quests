package dev.ftb.mods.ftbquests.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.ftb.mods.ftbquests.net.ClearRepeatCooldownMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.ProgressChange;

import java.util.Collection;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ChangeProgressCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("change_progress")
                .requires(FTBQuestsCommands::hasEditorPermission)
                .then(argument("players", EntityArgument.players())
                        .then(literal("reset")
                                .then(argument("quest_object", StringArgumentType.string())
                                        .executes(ctx -> {
                                            Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
                                            return changeProgress(ctx.getSource(), players, true, StringArgumentType.getString(ctx, "quest_object"));
                                        })
                                )
                        )
                        .then(literal("reset-all")
                                .executes(ctx -> {
                                    Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
                                    return changeProgress(ctx.getSource(), players, true, "1");
                                })
                        )
                        .then(literal("complete")
                                .then(argument("quest_object", StringArgumentType.string())
                                        .executes(ctx -> {
                                            Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
                                            return changeProgress(ctx.getSource(), players, false, StringArgumentType.getString(ctx, "quest_object"));
                                        })
                                )
                        )
                        .then(literal("complete-all")
                                .executes(ctx -> {
                                    Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
                                    return changeProgress(ctx.getSource(), players, false, "1");
                                })
                        )
                );
    }

    private static int changeProgress(CommandSourceStack source, Collection<ServerPlayer> players, boolean reset, String idStr) throws CommandSyntaxException {
        QuestObjectBase questObject = FTBQuestsCommands.getQuestObjectForString(idStr);
        for (ServerPlayer player : players) {
            ServerQuestFile.getInstance().getTeamData(player).ifPresent(data -> {
                ProgressChange progressChange = new ProgressChange(questObject, player.getUUID()).setReset(reset);
                questObject.forceProgress(data, progressChange);
                if (questObject instanceof Quest quest && reset) {
                    data.clearRepeatCooldown(quest);
                    ClearRepeatCooldownMessage.sendToTeam(quest, data.getTeamId());
                }
            });
        }

        source.sendSuccess(() -> Component.translatable("commands.ftbquests.change_progress.text"), false);
        return Command.SINGLE_SUCCESS;
    }
}
