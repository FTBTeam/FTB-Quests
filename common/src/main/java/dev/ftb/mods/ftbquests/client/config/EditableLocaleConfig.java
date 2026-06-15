package dev.ftb.mods.ftbquests.client.config;

import com.mojang.datafixers.util.Pair;
import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableConfigValue;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.screens.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleTextButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.List;

public class EditableLocaleConfig extends EditableConfigValue<String> {
    public static final Color4I COLOR_HI = Color4I.rgb(0xFFAA49);
    public static final Color4I COLOR_LO = Color4I.rgb(0x663D0F);

    @Override
    public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
        var gui = new LocaleSelectorScreen(callback);

        gui.setTitle(Component.translatable("ftbquests.xlate.editing_locale"));
        gui.showBottomPanel(false);  // no need for accept/cancel buttons here
        gui.setHasSearchBox(true);
        gui.openGui();
    }

    @Override
    public Color4I getColor(String v, Theme theme) {
        return theme.hasDarkBackground() ? COLOR_HI : COLOR_LO;
    }

    @Override
    public Component getStringForGUI(String v) {
        return Component.literal('"' + v + '"');
    }

    private class LocaleSelectorScreen extends AbstractButtonListScreen {
        private final ConfigCallback callback;
        private final List<Pair<String,Component>> entries = new ArrayList<>();
        private final int widest;

        public LocaleSelectorScreen(ConfigCallback callback) {
            this.callback = callback;

            MutableInt widestM = new MutableInt(0);

            entries.add(new Pair<>("", Component.translatable("ftbquests.gui.use_default_lang").withStyle(ChatFormatting.ITALIC)));
            Minecraft.getInstance().getLanguageManager().getLanguages().forEach((lang, info) -> {
                Component c = Component.literal("[" + lang + "] ").withStyle(ChatFormatting.YELLOW)
                        .append(info.toComponent().copy().withStyle(ChatFormatting.WHITE));
                entries.add(new Pair<>(lang, c));
                widestM.setValue(Math.max(widestM.toInteger(), getTheme().getStringWidth(c)));
            });

            widest = widestM.intValue();
        }

        @Override
        public boolean onInit() {
            setWidth(widest + 25);
            setHeight(getWindow().getGuiScaledHeight() * 4 / 5);
            return true;
        }

        @Override
        public void addButtons(Panel panel) {
            entries.forEach(entry -> {
                panel.add(new SimpleTextButton(panel, entry.getSecond(), Color4I.empty()) {
                    @Override
                    public void onClicked(MouseButton button) {
                        playClickSound();
                        boolean changed = updateValue(entry.getFirst());
                        callback.save(changed);
                    }
                });
            });
        }

        @Override
        protected void doCancel() {
            callback.save(false);
        }

        @Override
        protected void doAccept() {
            callback.save(true);
        }
    }
}
