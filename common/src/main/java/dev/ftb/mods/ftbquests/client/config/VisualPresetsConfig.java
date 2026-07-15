package dev.ftb.mods.ftbquests.client.config;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.config.ListConfig;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.gui.VisualPresetsEditorScreen;
import dev.ftb.mods.ftbquests.quest.preset.VisualPresets;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

public class VisualPresetsConfig extends ConfigValue<VisualPresets> {
    @Override
    public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
        var gui = new VisualPresetsEditorScreen(this, callback);
        gui.setTitle(Component.translatable("ftbquests.file.presets"));
        gui.openGui();
    }

    @Override
    public Component getStringForGUI(@Nullable VisualPresets v) {
        return v == null ? NULL_TEXT : formatListSize(v);
    }

    private Component formatListSize(VisualPresets v) {
        MutableComponent main = v.allPresets().size() == 1 ? Component.translatable("ftblibrary.gui.listSize1") : Component.translatable("ftblibrary.gui.listSize", v.allPresets().size());
        return Component.literal("[ ").append(main.withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY)).append(" ]");
    }

    @Override
    public Color4I getColor(@Nullable VisualPresets v) {
        return ListConfig.COLOR;
    }

}
