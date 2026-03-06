package dev.ftb.mods.ftbquests.command;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.net.SyncEditorPermissionMessage;
import dev.ftb.mods.ftbquests.net.SyncQuestsMessage;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.commands.Commands.literal;

public class ReloadCommand {
    private static final Set<UUID> warnedPlayers = new HashSet<>();

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("reload")
                .requires(FTBQuestsCommands::hasEditorPermission)
                .executes(context -> doReload(context.getSource(), true, true))
                .then(literal("quests")
                        .executes(context -> doReload(context.getSource(), true, false))
                )
                .then(literal("team_progress")
                        .executes(context -> doReload(context.getSource(), false, true))
                );
    }

    private static int doReload(CommandSourceStack source, boolean quests, boolean progression) {
        if (!quests && !progression) {
            return 0;
        }

        ServerQuestFile instance = ServerQuestFile.getInstance();
        ServerPlayer sender = source.getPlayer();

        instance.load(quests, progression);
        NetworkHelper.sendToAll(source.getServer(), new SyncQuestsMessage(instance));
        source.getServer().getPlayerList().getPlayers().forEach(p -> {
            NetworkManager.sendToPlayer(p, SyncEditorPermissionMessage.forPlayer(p));
            instance.getTranslationManager().sendTranslationsToPlayer(p);
        });

        String suffix = quests && progression ? "" : (quests ? "_quest" : "_progress");
        source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.reloaded" + suffix), false);
        UUID id = sender == null ? Util.NIL_UUID : sender.getUUID();
        if (!warnedPlayers.contains(id)) {
            source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.reloaded.disclaimer")
                    .withStyle(ChatFormatting.GOLD), false);
            warnedPlayers.add(id);
        }

        return Command.SINGLE_SUCCESS;
    }


}
