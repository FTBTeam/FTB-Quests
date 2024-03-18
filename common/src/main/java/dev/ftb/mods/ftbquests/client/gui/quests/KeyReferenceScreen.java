package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class KeyReferenceScreen extends BaseScreen {
    private final Panel textPanel;
    private final PanelScrollBar scrollBar;
    private final SimpleTextButton closeButton;
    private final String[] translationKeys;

    private static final int SCROLLBAR_WIDTH = 16;
    private static final int GUTTER_SIZE = 2;

    public KeyReferenceScreen(String... translationKeys) {
        this.translationKeys = translationKeys;

        textPanel = new TextPanel(this);

        closeButton = new SimpleTextButton(this, Component.translatable("gui.close"), Icons.CLOSE) {
            @Override
            public void onClicked(MouseButton button) {
                onBack();
            }
        };
        scrollBar = new PanelScrollBar(this, textPanel);
    }

    @Override
    public boolean onInit() {
        return setSizeProportional(0.75f, 0.8f);
    }

    @Override
    public Theme getTheme() {
        return EditConfigScreen.THEME;
    }

    @Override
    public void addWidgets() {
        add(textPanel);
        add(scrollBar);
        add(closeButton);
    }

    @Override
    public void alignWidgets() {
        int textPanelWidth = getGui().width - GUTTER_SIZE * 3 - SCROLLBAR_WIDTH;

        textPanel.setPosAndSize(GUTTER_SIZE, GUTTER_SIZE, textPanelWidth, getGui().height - GUTTER_SIZE * 2);
        textPanel.alignWidgets();

        scrollBar.setPosAndSize(getGui().width - GUTTER_SIZE - SCROLLBAR_WIDTH, textPanel.getPosY(), SCROLLBAR_WIDTH, textPanel.getHeight());

        closeButton.setPosAndSize(width + 2, 0, 20, 20);
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        Color4I.DARK_GRAY.draw(graphics, x, y, w, h);

        Component msg = Component.translatable("ftbquests.gui.key_reference");
        int w1 = theme.getStringWidth(msg);
        theme.drawString(graphics, msg, x + (w - w1) / 2, y - theme.getFontHeight() - 1, Color4I.rgb(0x00FFFF), Theme.SHADOW);
    }

    private static List<Pair<Component, Component>> buildText(String... translationKeys) {
        List<Pair<Component,Component>> res = new ArrayList<>();
        for (String translationKey: translationKeys) {
            for (String line : I18n.get(translationKey).split("\\n")) {
                String[] parts = line.split(";", 2);
                switch (parts.length) {
                    case 0 -> res.add(Pair.of(Component.empty(), Component.empty()));
                    case 1 -> res.add(Pair.of(Component.literal(parts[0]).withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE), Component.empty()));
                    default -> res.add(Pair.of(Component.literal(parts[0]), Component.literal(parts[1]).withStyle(ChatFormatting.GRAY)));
                }
            }
            res.add(Pair.of(Component.empty(), Component.empty()));
        }
        return res;
    }

    private class TextPanel extends Panel {
        private final TwoColumnList textWidget;

        public TextPanel(Panel panel) {
            super(panel);

            textWidget = new TwoColumnList(this, buildText(translationKeys));
        }

        @Override
        public void addWidgets() {
            add(textWidget);
        }

        @Override
        public void alignWidgets() {
            align(WidgetLayout.VERTICAL);

            textWidget.setPos(4, 2);
            textWidget.setWidth(width);
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            ThemeProperties.BACKGROUND.get().draw(graphics, x, y, w, h);
        }
    }

    private static class TwoColumnList extends Widget {
        private final int widestL;
        private final List<Pair<Component, Component>> data;
        private final List<Pair<Component, FormattedCharSequence>> reflowed = new ArrayList<>();

        public TwoColumnList(Panel p, List<Pair<Component, Component>> data) {
            super(p);

            this.data = data;
            this.widestL = data.stream()
                    .map(e -> getGui().getTheme().getStringWidth(e.getLeft()))
                    .max(Integer::compareTo)
                    .orElse(0);
        }

        @Override
        public void setWidth(int v) {
            super.setWidth(v);

            reflowText();
        }

        private void reflowText() {
            Theme theme = getGui().getTheme();
            int h = 0;
            int maxWidth = getParent().getWidth() - GUTTER_SIZE * 2;
            reflowed.clear();
            for (var entry : data) {
                if (entry.getRight().getString().isEmpty()) {
                    // header line
                    reflowed.add(Pair.of(entry.getLeft(), null));
                    h += theme.getFontHeight() + 3;
                } else {
                    var l = theme.getFont().split(entry.getRight(), maxWidth - 10 - widestL);
                    if (!l.isEmpty()) {
                        reflowed.add(Pair.of(entry.getLeft(), l.get(0)));
                        for (int i = 1; i < l.size(); i++) {
                            reflowed.add(Pair.of(Component.empty(), l.get(i)));
                        }
                        h += (theme.getFontHeight() + 1) * l.size();
                    }
                }
            }

            height = h;
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            int yPos = y;

            for (var entry : reflowed) {
                boolean header = entry.getRight() == null;
                int leftWidth = theme.getStringWidth(entry.getLeft());
                int xOff = header ? (width - leftWidth) / 2 : widestL - leftWidth - 2;
                theme.drawString(graphics, entry.getLeft(), x + xOff, yPos);
                if (!header) {
                    theme.drawString(graphics, entry.getRight(), x + widestL + 10, yPos);
                }
                yPos += theme.getFontHeight() + (header ? 3 : 1);
            }
        }
    }
}
