package dev.ftb.mods.ftbquests.client.config;

import com.mojang.datafixers.util.Pair;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LocaleConfig extends ConfigValue<String> {
    public static final Color4I COLOR = Color4I.rgb(0xFFAA49);

    private final LocaleValue localeValue;

    public LocaleConfig(LocaleValue localeValue) {
        this.localeValue = localeValue;
    }

    @Override
    public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
        var gui = new LocaleSelectorScreen(localeValue, callback);

        gui.setTitle(Component.translatable("ftbquests.xlate.editing_locale"));
        gui.showBottomPanel(false);  // no need for accept/cancel buttons here
        gui.setHasSearchBox(true);
        gui.openGui();
    }

    @Override
    public Color4I getColor(@Nullable String v) {
        return COLOR;
    }

    @Override
    public Component getStringForGUI(@Nullable String v) {
        return v == null ? NULL_TEXT : Component.literal('"' + v + '"');
    }

    private class LocaleSelectorScreen extends AbstractButtonListScreen {
        private final LocaleValue localeValue;
        private final ConfigCallback callback;
        private final List<Pair<String,Component>> entries = new ArrayList<>();
        private final int widest;

        public LocaleSelectorScreen(LocaleValue localeValue, ConfigCallback callback) {
            this.localeValue = localeValue;
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
            setHeight(getScreen().getGuiScaledHeight() * 4 / 5);
            return true;
        }

        @Override
        public void addButtons(Panel panel) {
            entries.forEach(entry -> {
                panel.add(new SimpleTextButton(panel, entry.getSecond(), Color4I.empty()) {
                    @Override
                    public void onClicked(MouseButton button) {
                        playClickSound();
                        boolean changed = setCurrentValue(entry.getFirst());
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
