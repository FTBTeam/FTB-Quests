package dev.ftb.mods.ftbquests.client.config;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.Tristate;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableConfigValue;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableString;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.client.gui.DepLineVisibilityScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Map;

public class EditableDepLineVisibility extends EditableConfigValue<Map<Long, Tristate>> {
    public EditableDepLineVisibility(Quest quest) {
        setValue(quest.depLineVisibilityAsTristateMap());
    }

    @Override
    public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
        new DepLineVisibilityScreen(this, callback).openGui();
    }

    @Override
    public Color4I getColor(Map<Long, Tristate> value, Theme theme) {
        return theme.hasDarkBackground() ? EditableString.COLOR_HI : EditableString.COLOR_LO;
    }

    @Override
    public Component getStringForGUI(Map<Long, Tristate> value) {
        return Component.literal("< ").append(Component.translatable("ftbquests.gui.edit").withStyle(ChatFormatting.ITALIC)).append(" >");
    }
}
