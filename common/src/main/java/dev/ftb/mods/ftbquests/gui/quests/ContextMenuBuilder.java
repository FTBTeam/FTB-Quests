package dev.ftb.mods.ftbquests.gui.quests;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContextMenuBuilder {
    private final QuestObjectBase object;
    private final QuestScreen screen;
    private Movable deletionFocus = null;
    private final List<ContextMenuItem> atTop = new ArrayList<>();
    private final List<ContextMenuItem> atBottom = new ArrayList<>();

    private ContextMenuBuilder(QuestObjectBase object, QuestScreen screen) {
        this.object = object;
        this.screen = screen;
    }

    public static ContextMenuBuilder create(QuestObjectBase object, QuestScreen screen) {
        return new ContextMenuBuilder(object, screen);
    }

    public ContextMenuBuilder withDeletionFocus(Movable m) {
        deletionFocus = m;
        return this;
    }

    public ContextMenuBuilder insertAtTop(Collection<ContextMenuItem> toAdd) {
        atTop.addAll(toAdd);
        return this;
    }

    public ContextMenuBuilder insertAtBottom(Collection<ContextMenuItem> toAdd) {
        atBottom.addAll(toAdd);
        return this;
    }

    public void openContextMenu(BaseScreen gui) {
        gui.openContextMenu(build(gui));
    }

    public List<ContextMenuItem> build(BaseScreen gui) {
        List<ContextMenuItem> res = new ArrayList<>();

        res.add(new ContextMenuItem(Component.literal("\"").append(object.getTitle()).append("\""), Color4I.EMPTY, null).setCloseMenu(false));
        res.add(ContextMenuItem.SEPARATOR);
        res.addAll(atTop);
        screen.addObjectMenuItems(res, gui, object, deletionFocus);
        res.addAll(atBottom);

        return List.copyOf(res);
    }
}
