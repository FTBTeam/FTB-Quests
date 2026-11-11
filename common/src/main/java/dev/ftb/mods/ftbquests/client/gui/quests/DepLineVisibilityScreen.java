package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.TextField;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.config.EditableDepLineVisibility;
import dev.ftb.mods.ftbquests.client.gui.CyclerButton;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.*;

public class DepLineVisibilityScreen extends AbstractButtonListScreen {
    private final EditableDepLineVisibility config;
    private final ConfigCallback callback;
    private final Map<Long, Tristate> editingMap;
    private final List<Quest> depQuests;
    private final int widestName;

    public DepLineVisibilityScreen(EditableDepLineVisibility config, ConfigCallback callback) {
        this.config = config;
        this.callback = callback;

        editingMap = new HashMap<>(config.getValue());

        ClientQuestFile cqf = ClientQuestFile.getInstance();
        depQuests = editingMap.keySet().stream()
                .map(cqf::getQuest)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(q -> q.getTitle().getString()))
                .toList();

        widestName = depQuests.stream()
                .map(q -> getTheme().getFont().width(q.getTitle()))
                .max(Integer::compareTo)
                .orElse(150);

        setTitle(Component.translatable("ftbquests.gui.dependency_line_visibility"));
    }

    @Override
    public void addButtons(Panel panel) {
        depQuests.forEach(quest -> panel.add(new Entry(panel, quest, editingMap.get(quest.getId()))));
    }

    @Override
    protected void doCancel() {
        closeGui();
    }

    @Override
    protected void doAccept() {
        config.setValue(editingMap);
        callback.save(true);
        closeGui();
    }

    @Override
    public boolean onInit() {
        setSize(
                Math.max(getTheme().getStringWidth(getTitle()), widestName + 50),
                Math.min(20 * (editingMap.size() + 1) + 50, getWindow().getGuiScaledHeight() * 3 / 4)
        );
        return true;
    }

    private class Entry extends Panel {
        private final Quest depQuest;
        private final Tristate val;
        private TextField nameField;
        private Button button;

        public Entry(Panel parent, Quest quest, Tristate val) {
            super(parent);
            this.depQuest = quest;
            this.val = val;
        }

        @Override
        public void addWidgets() {
            add(nameField = new TextField(this) {
                @Override
                public boolean mousePressed(MouseButton mb) {
                    if (isMouseOver()) {
                        button.onClicked(mb);
                        return true;
                    }
                    return super.mousePressed(mb);
                }
            }.setText(depQuest.getTitle()));
            add(button = new CyclerButton<>(this, Tristate.NAME_MAP, val, newVal -> editingMap.put(depQuest.getId(), newVal)));
        }

        @Override
        public void alignWidgets() {
            setSize(parent.width - 4, 16);
            nameField.setPosAndSize(widestName - nameField.width + 10, (16 - nameField.height) / 2, widestName, nameField.height);
            button.setPosAndSize(nameField.width + 15, 0, 16, 16);
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            graphics.hLine(x, x + w, y + h, Color4I.GRAY.withAlpha(30).rgba());
        }
    }
}
