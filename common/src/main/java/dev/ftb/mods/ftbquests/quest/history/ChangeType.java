package dev.ftb.mods.ftbquests.quest.history;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

public enum ChangeType {
    INITIAL("", ChatFormatting.WHITE, "", Color4I.empty()),
    UNDO("↶", ChatFormatting.DARK_GREEN, "ftbquests.gui.undo", Icons.UNDO),
    REDO("↷", ChatFormatting.DARK_AQUA, "ftbquests.gui.redo", Icons.REDO);

    private final String symbol;
    private final ChatFormatting color;
    private final String xlate;
    private final Icon icon;

    ChangeType(String symbol, ChatFormatting color, String xlate, Icon icon) {
        this.symbol = symbol;
        this.color = color;
        this.xlate = xlate;
        this.icon = icon;
    }

    public Component description() {
        return this == INITIAL ?
                Component.empty() :
                ComponentUtils.wrapInSquareBrackets(Component.literal(symbol + " ").append(Component.translatable(xlate))).append(" ").withStyle(color);
    }

    public Component simpleDescription() {
        return Component.translatable(xlate);
    }

    public Icon icon() {
        return icon;
    }

    public boolean isUndo() {
        return this == UNDO;
    }
}
