package dev.ftb.mods.ftbquests.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.DoubleConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditStringConfigOverlay;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.util.TextComponentUtils;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.config.VisualPresetsConfig;
import dev.ftb.mods.ftbquests.quest.QuestShape;
import dev.ftb.mods.ftbquests.quest.preset.VisualPreset;
import dev.ftb.mods.ftbquests.quest.preset.VisualPresetRecord;
import dev.ftb.mods.ftbquests.quest.preset.VisualPresets;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class VisualPresetsEditorScreen extends AbstractButtonListScreen {
    private final Map<String, VisualPresetRecord> editingMap;
    private final VisualPresetsConfig config;
    private final ConfigCallback callback;
    private int widestName = 40;
    private int widestShapeName = 40;

    public VisualPresetsEditorScreen(VisualPresetsConfig config, ConfigCallback callback) {
        this.config = config;
        this.callback = callback;
        ClientQuestFile cqf = ClientQuestFile.INSTANCE;
        editingMap = new HashMap<>(cqf.getPresets().allPresets());

        for (Map.Entry<String, VisualPresetRecord> entry : editingMap.entrySet()) {
            String name = entry.getKey();
            VisualPreset preset = entry.getValue();

            widestName = Math.max(widestName, getGui().getTheme().getStringWidth(name));
            widestShapeName = Math.max(widestShapeName, getGui().getTheme().getStringWidth(QuestShape.idMap.getDisplayName(preset.shapeName())));
        }

        widestName += 5;
        widestShapeName += 20;
    }

    @Override
    protected Panel createTopPanel() {
        return new CustomTopPanel();
    }

    @Override
    public void addButtons(Panel panel) {
        panel.add(new PresetTitle(panel));
        editingMap.keySet().stream().sorted().forEach(name -> {
            var preset = editingMap.get(name);
            panel.add(new PresetEntry(panel, name, preset));
        });
    }

    @Override
    public boolean onInit() {
        setSize(widestName + widestShapeName + 96, Math.min(20 * (editingMap.size() + 1) + 50, getWindow().getGuiScaledHeight() * 3 / 4));
        return true;
    }

    @Override
    protected void doCancel() {
        closeGui();
    }

    @Override
    protected void doAccept() {
        config.setValue(new VisualPresets(editingMap));
        callback.save(true);
        closeGui();
    }

    private class PresetTitle extends Panel {
        private TextField name, shape, size;

        public PresetTitle(Panel panel) {
            super(panel);
        }

        @Override
        public void addWidgets() {
            add(name = new TextField(this).setText(Component.empty()));
            add(shape = new TextField(this).setText(Component.translatable("ftbquests.quest.appearance.shape")));
            add(size = new TextField(this).setText(Component.translatable("ftbquests.quest.appearance.size")));
        }

        @Override
        public void alignWidgets() {
            setSize(width - 4, 16);
            name.setPosAndSize(widestName - getTheme().getStringWidth(name.getTitle()) - 2, 1, widestName, name.height);
            shape.setPosAndSize(name.width + 10, 1, widestShapeName, 18);
            size.setPosAndSize(shape.posX + shape.width + 5, 1, 30, size.height);
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            super.draw(graphics, theme, x, y, w, h);
            graphics.hLine(x, x + w, y + h - 3, Color4I.GRAY.withAlpha(128).rgba());
        }
    }

    private class PresetEntry extends Panel {
        private final String presetName;
        private final VisualPreset preset;
        private TextField nameField;
        private ShapeButton shapeButton;
        private SizeButton sizeButton;
        private Button editButton;
        private Button delButton;

        public PresetEntry(Panel parent, String presetName, VisualPreset preset) {
            super(parent);

            this.presetName = presetName;
            this.preset = preset;
        }

        @Override
        public void addWidgets() {
            add(nameField = new TextField(this).setText(Component.literal(presetName).withStyle(ChatFormatting.YELLOW)).addFlags(Theme.SHADOW));
            add(shapeButton = new ShapeButton(this, preset.shapeName(), newShapeName -> {
                double size = editingMap.get(presetName).size();
                editingMap.put(presetName, new VisualPresetRecord(newShapeName, size));
                shapeButton.setTitle(QuestShape.idMap.getDisplayName(newShapeName).copy().append(" ▼"));
                shapeButton.setWidth(widestShapeName);
            }));
            add(sizeButton = new SizeButton(this, presetName, preset.size()));
            add(editButton = new EditDelButton(this, Component.translatable("ftbquests.gui.edit"), Icon.getIcon("ftbquests:textures/gui/editor.png"),
                    (btn, mb) ->
                            getGui().pushModalPanel(new AddEditPresetOverlay(getGui(), presetName)))
            );
            add(delButton = new EditDelButton(this, Component.translatable("gui.remove"), Icons.BIN,
                    (btn, mb) -> {
                        editingMap.remove(presetName);
                        VisualPresetsEditorScreen.this.refreshWidgets();
                    }));
        }

        @Override
        public void alignWidgets() {
            setX(2);
            setSize(width - 4, 20);
            nameField.setPosAndSize(widestName - getTheme().getStringWidth(presetName) - 2, (height - getTheme().getFontHeight()) / 2 + 1, widestName, nameField.height);
            shapeButton.setPosAndSize(nameField.width + 5, 1, widestShapeName, 18);
            sizeButton.setPosAndSize(shapeButton.posX + shapeButton.width + 5, 1, 30, sizeButton.height);
            editButton.setPosAndSize(sizeButton.posX + sizeButton.width + 5, 4, 12, 12);
            delButton.setPosAndSize(editButton.posX + editButton.width + 2, 4, 12, 12);
        }
    }

    private class EditDelButton extends SimpleButton {
        public EditDelButton(Panel panel, Component text, Icon icon, Callback c) {
            super(panel, text, icon, c);
        }

        @Override
        public boolean shouldDraw() {
            return getMouseX() > parent.getX() && getMouseX() < parent.getX() + parent.width
                    && getMouseY() > parent.getY() && getMouseY() < parent.getY() + parent.height;
        }
    }


    private class SizeButton extends SimpleTextButton {
        private final String name;

        public SizeButton(Panel panel, String name, double size) {
            super(panel, Component.literal(String.format("%.2f", size)), Icon.empty());
            this.name = name;
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        }

        @Override
        public void onClicked(MouseButton button) {
            DoubleConfig config = new DoubleConfig(0.1, 10.0);
            VisualPreset preset = editingMap.get(name);
            config.setValue(preset.size());
            EditStringConfigOverlay<Double> overlay = new EditStringConfigOverlay<>(getGui(), config, accepted -> {
                if (accepted) {
                    String shapeName = preset.shapeName();
                    editingMap.put(name, new VisualPresetRecord(shapeName, config.getValue()));
                    setTitle(Component.literal(String.format("%.2f", config.getValue())));
                }
            }).atMousePosition();
            overlay.setWidth(100);
            overlay.setExtraZlevel(200);
            getGui().pushModalPanel(overlay);
        }
    }

    private static class ShapeButton extends SimpleTextButton {
        private final Consumer<String> contextMenuAction;

        public ShapeButton(Panel panel, String shapeName, Consumer<String> contextMenuAction) {
            super(panel, QuestShape.idMap.getDisplayName(shapeName).copy().append(" ▼"), Icon.empty());
            this.contextMenuAction = contextMenuAction;
        }

        @Override
        public void onClicked(MouseButton button) {
            List<ContextMenuItem> items = new ArrayList<>();
            for (String shapeName : QuestShape.idMap.keys) {
                items.add(new ContextMenuItem(QuestShape.idMap.getDisplayName(shapeName), Icon.empty(), b -> {}) {
                    @Override
                    public void onClicked(Button button, Panel panel, MouseButton mouseButton) {
                        contextMenuAction.accept(shapeName);
                        super.onClicked(button, panel, mouseButton);
                    }
                });
            }
            getGui().openContextMenu(items);
        }
    }

    private class CustomTopPanel extends ButtonListTopPanel {
        Button addPresetButton;

        @Override
        public void addWidgets() {
            super.addWidgets();

            add(addPresetButton = new SimpleButton(this, Component.translatable("gui.add"), Icons.ADD, (b, mb) -> {
                getGui().pushModalPanel(new AddEditPresetOverlay(getGui()));
            }));
        }

        @Override
        public void alignWidgets() {
            super.alignWidgets();

            addPresetButton.setPosAndSize(width - 16, (height - 12) / 2, 12, 12);
        }
    }

    private class AddEditPresetOverlay extends ModalPanel {
        private TextField name, shape, size;
        private TextBox nameField;
        private ShapeButton shapeButton;
        private TextBox sizeField;
        private final Button accept = SimpleTextButton.accept(this, this::onAccepted, TextComponentUtils.hotkeyTooltip("Enter"));
        private final Button cancel = SimpleTextButton.cancel(this, this::onCancelled, TextComponentUtils.hotkeyTooltip("ESC"));
        private String shapeName = "square";
        @Nullable
        private final String editingPreset;
        @Nullable
        private final VisualPreset editingRec;

        public AddEditPresetOverlay(Panel panel) {
            this(panel, null);
        }

        public AddEditPresetOverlay(Panel panel, @Nullable String editingPreset) {
            super(panel);

            this.editingPreset = editingPreset;
            this.editingRec = editingPreset == null ? null : editingMap.get(editingPreset);
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            theme.drawGui(graphics, x, y, w, h, WidgetType.NORMAL);
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            super.draw(graphics, theme, x, y, w, h);

            Component c = Component.translatable(editingPreset == null ? "gui.add" : "ftbquests.gui.edit");
            theme.drawString(graphics, c, x + (width - theme.getStringWidth(c)) / 2, y + 3);
        }

        @Override
        public void addWidgets() {
            add(name = new TextField(this).setText(Component.translatable("ftbquests.quest.appearance.preset")));
            add(shape = new TextField(this).setText(Component.translatable("ftbquests.quest.appearance.shape")));
            add(size = new TextField(this).setText(Component.translatable("ftbquests.quest.appearance.size")));

            add(nameField = new TextBox(this) {
                @Override
                public boolean allowInput() {
                    return editingPreset == null && super.allowInput();
                }
            });
            nameField.setFilter(s -> s.isEmpty() || s.matches("^\\w+$"));
            nameField.ghostText = "Preset name";

            if (editingPreset != null && editingRec != null) {
                nameField.setText(editingPreset);
                shapeName = editingRec.shapeName();
            }

            add(shapeButton = new ShapeButton(this, shapeName, newShapeName -> {
                shapeName = newShapeName;
                shapeButton.setTitle(QuestShape.idMap.getDisplayName(newShapeName).copy().append(" ▼"));
                shapeButton.setWidth(widestShapeName);
            }));

            add(sizeField = new TextBox(this));
            sizeField.setFilter(s -> parseDouble(s).isPresent());
            sizeField.setText((editingRec == null) ? "1.0" : String.format("%.2f", editingRec.size()));

            add(accept);
            add(cancel);
        }

        @Override
        public void alignWidgets() {
            setSize(200, 100);

            name.setPos(85 - name.width, 20);
            nameField.setPosAndSize(90, 17, 100, 16);

            shape.setPos(85 - shape.width, 40);
            shapeButton.setPosAndSize(90, 37, widestShapeName, 16);

            size.setPos(85 - size.width, 60);
            sizeField.setPosAndSize(90, 57, 100, 16);

            cancel.setPos(width - cancel.width - 2, height - 22);
            accept.setPos(cancel.posX - accept.width - 2, height - 22);
        }

        @Override
        public boolean keyPressed(Key key) {
            if (key.enter() || key.is(InputConstants.KEY_NUMPADENTER)) {
                onAccepted(MouseButton.LEFT);
                return true;
            }
            return super.keyPressed(key);
        }

        private OptionalDouble parseDouble(String s) {
            if (s.isEmpty() || s.endsWith(".")) {
                s = s + "0";  // allows for "" or e.g. "123."
            }
            if (NumberUtils.isParsable(s)) {
                return OptionalDouble.of(Double.parseDouble(s));
            }
            return OptionalDouble.empty();
        }

        private void onAccepted(MouseButton mouseButton) {
            try {
                String presetName = nameField.getText();
                if (presetName.isEmpty()) {
                    throw new IllegalArgumentException("Preset name can't be empty");
                }
                double size = parseDouble(sizeField.getText()).orElse(1.0);
                if (size >= 0.1 && size < 10.0) {
                    editingMap.put(presetName, new VisualPresetRecord(shapeName, size));
                    getGui().popModalPanel();
                    VisualPresetsEditorScreen.this.refreshWidgets();
                } else {
                    throw new IllegalArgumentException("Size must be >= 0.1 && <= 10.0");
                }
            } catch (RuntimeException e) {
                FTBQuestsClient.showInfoToast(Component.literal(e.getMessage()), Icons.BARRIER, Component.empty());
            }
        }

        private void onCancelled(MouseButton mouseButton) {
            getGui().popModalPanel();
        }
    }
}
