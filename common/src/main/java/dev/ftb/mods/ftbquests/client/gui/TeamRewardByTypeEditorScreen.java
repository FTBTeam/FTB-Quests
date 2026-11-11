package dev.ftb.mods.ftbquests.client.gui;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.Tristate;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.screens.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Button;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.TextField;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.client.config.EditableTeamRewardByType;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class TeamRewardByTypeEditorScreen extends AbstractButtonListScreen {
    private final EditableTeamRewardByType config;
    private final ConfigCallback callback;
    private final Map<RewardType, Tristate> editingMap;
    private int widestName = 40;

    public TeamRewardByTypeEditorScreen(EditableTeamRewardByType config, ConfigCallback callback) {
        this.config = config;
        this.callback = callback;

        editingMap = new HashMap<>();
        RewardTypes.TYPES.keySet().forEach(id -> {
            Tristate val = config.getValue().containsKey(id) ? Tristate.ofBoolean(config.getValue().get(id)) : Tristate.DEFAULT;
            RewardType type = RewardTypes.TYPES.get(id);
            if (type != null) {
                editingMap.put(type, val);
                widestName = Math.max(widestName, getGui().getTheme().getStringWidth(type.getDisplayName()));
            }
        });
    }

    @Override
    public void addButtons(Panel panel) {
        editingMap.keySet().stream().sorted(Comparator.comparing(k -> k.getDisplayName().getString()))
                .forEach(type -> panel.add(new Entry(panel, type, editingMap.get(type))));
    }

    @Override
    protected void doCancel() {
        closeGui();
    }

    @Override
    protected void doAccept() {
        Map<Identifier, Boolean> map = new HashMap<>();
        editingMap.forEach((type, val) -> {
            if (!val.isDefault()) {
                map.put(type.getTypeId(), val.isTrue());
            }
        });
        config.setValue(map);
        callback.save(true);
        closeGui();
    }

    @Override
    public boolean onInit() {
        setSize(200, Math.min(20 * (editingMap.size() + 1) + 50, getWindow().getGuiScaledHeight() * 3 / 4));
        return true;
    }

    private class Entry extends Panel {
        private final RewardType type;
        private final Tristate val;
        private TextField nameField;
        private Button button;

        public Entry(Panel parent, RewardType type, Tristate val) {
            super(parent);
            this.type = type;
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
            }.setText(type.getDisplayName()));
            add(button = new CyclerButton<>(this, Tristate.NAME_MAP, val, newVal -> editingMap.put(type, newVal)));
        }

        @Override
        public void alignWidgets() {
            setSize(parent.width - 4, 16);
            nameField.setPosAndSize(widestName - nameField.width + 10, (16 - nameField.height) / 2, widestName, nameField.height);
            button.setPosAndSize(nameField.width + 15, 0, 16, 16);
        }

        @Override
        public void drawBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
            graphics.horizontalLine(x, x + w, y + h, Color4I.GRAY.withAlpha(30).rgba());
        }
    }
}
