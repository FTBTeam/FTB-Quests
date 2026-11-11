package dev.ftb.mods.ftbquests.client.config;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.gui.quests.DepLineVisibilityScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EditableDepLineVisibility extends ConfigValue<Map<Long, Tristate>> {
    public EditableDepLineVisibility(Quest quest) {
        setValue(quest.depLineVisibilityAsTristateMap());
    }

    @Override
    public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
        new DepLineVisibilityScreen(this, callback).openGui();
    }

    @Override
    public Color4I getColor(@Nullable Map<Long, Tristate> v) {
        return StringConfig.COLOR;
    }

    @Override
    public Component getStringForGUI(Map<Long, Tristate> value) {
        return Component.literal("< ").append(Component.translatable("ftbquests.gui.edit").withStyle(ChatFormatting.ITALIC)).append(" >");
    }
}
