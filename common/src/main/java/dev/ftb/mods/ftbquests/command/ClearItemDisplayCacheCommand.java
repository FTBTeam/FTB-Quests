package dev.ftb.mods.ftbquests.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.mods.ftbquests.net.ClearDisplayCacheMessage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ClearItemDisplayCacheCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("clear_item_display_cache")
                .requires(FTBQuestsCommands::hasEditorPermission)
                .executes(c -> clearDisplayCache(c.getSource()));
    }

    private static int clearDisplayCache(CommandSourceStack source) {
        ClearDisplayCacheMessage.clearForAll();
        source.sendSuccess(() -> Component.translatable("commands.ftbquests.command.feedback.clear_display_cache"), false);
        return Command.SINGLE_SUCCESS;
    }
}
