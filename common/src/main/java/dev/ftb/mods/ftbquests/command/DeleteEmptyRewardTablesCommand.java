package dev.ftb.mods.ftbquests.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;

public class DeleteEmptyRewardTablesCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("delete_empty_reward_tables")
                .requires(FTBQuestsCommands::hasEditorPermission)
                .executes(context -> deleteEmptyRewardTables(context.getSource()));
    }

    private static int deleteEmptyRewardTables(CommandSourceStack source) {
        int removed = ServerQuestFile.getInstance().removeEmptyRewardTables(source);

        source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.delete_empty_reward_tables.text", removed), false);

        return Command.SINGLE_SUCCESS;
    }
}
