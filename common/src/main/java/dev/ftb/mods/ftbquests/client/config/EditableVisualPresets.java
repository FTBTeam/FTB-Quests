package dev.ftb.mods.ftbquests.client.config;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableConfigValue;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableString;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.client.gui.VisualPresetsEditorScreen;
import dev.ftb.mods.ftbquests.quest.preset.VisualPresets;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

public class EditableVisualPresets extends EditableConfigValue<VisualPresets> {
    @Override
    public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
        var gui = new VisualPresetsEditorScreen(this, callback);
        gui.setTitle(Component.translatable("ftbquests.file.presets"));
        gui.openGui();
    }

    @Override
    public Component getStringForGUI(@Nullable VisualPresets v) {
        return v == null ? Component.literal("<null>") : formatListSize(v);
    }

    private Component formatListSize(VisualPresets v) {
        MutableComponent main = v.allPresets().size() == 1 ? Component.translatable("ftblibrary.gui.listSize1") : Component.translatable("ftblibrary.gui.listSize", v.allPresets().size());
        return Component.literal("[ ").append(main.withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY)).append(" ]");
    }

    @Override
    public Color4I getColor(VisualPresets value, Theme theme) {
        return theme.hasDarkBackground() ? EditableString.COLOR_HI : EditableString.COLOR_LO;
    }


}
