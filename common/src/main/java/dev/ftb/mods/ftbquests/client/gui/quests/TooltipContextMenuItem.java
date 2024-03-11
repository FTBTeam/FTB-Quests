package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class TooltipContextMenuItem extends ContextMenuItem {
    private final List<Component> tooltips;

    public TooltipContextMenuItem(Component title, Icon icon, @Nullable Consumer<Button> callback, Component... tooltips) {
        super(title, icon, callback);
        this.tooltips = Arrays.asList(tooltips);
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        tooltips.forEach(list::add);
    }
}
