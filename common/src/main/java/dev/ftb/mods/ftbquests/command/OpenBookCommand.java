package dev.ftb.mods.ftbquests.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftbquests.net.OpenQuestBookMessage;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;

import org.jspecify.annotations.Nullable;

import static net.minecraft.commands.Commands.argument;

public class OpenBookCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("open_book")
                .executes(c -> openQuest(c.getSource().getPlayerOrException(), null))
                .then(argument("quest_object", StringArgumentType.string())
                        .executes(c -> openQuest(c.getSource().getPlayerOrException(), StringArgumentType.getString(c, "quest_object")))
                );
    }

    private static int openQuest(ServerPlayer player, @Nullable String qobId) throws CommandSyntaxException {
        if (qobId == null) {
            NetworkManager.sendToPlayer(player, OpenQuestBookMessage.lastOpenedQuest());
            return Command.SINGLE_SUCCESS;
        } else {
            if (FTBQuestsCommands.getQuestObjectForString(qobId) instanceof QuestObject qo && playerCanSeeQuestObject(player, qo)) {
                NetworkManager.sendToPlayer(player, new OpenQuestBookMessage(qo.id));
                return Command.SINGLE_SUCCESS;
            }
        }
        return 0;
    }

    private static boolean playerCanSeeQuestObject(ServerPlayer player, QuestObject qo) {
        if (qo instanceof Chapter) {
            return true;
        }
        return ServerQuestFile.getInstance().getTeamData(player).map(data -> {
            Quest quest = qo.getRelatedQuest();
            return quest != null && (data.getCanEdit(player) || !quest.hideDetailsUntilStartable() || data.canStartTasks(quest));
        }).orElse(false);
    }
}
