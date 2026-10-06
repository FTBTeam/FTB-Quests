package dev.ftb.mods.ftbquests.client.config;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableConfigValue;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableString;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.client.gui.TeamRewardByTypeEditorScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Map;

public class EditableTeamRewardByType extends EditableConfigValue<Map<Identifier, Boolean>> {
    @Override
    public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
        var gui = new TeamRewardByTypeEditorScreen(this, callback);
        gui.setTitle(Component.translatable("ftbquests.file.defaults.reward_team_by_type"));
        gui.openGui();
    }

    @Override
    public Component getStringForGUI(Map<Identifier, Boolean> value) {
        return Component.literal("< ").append(Component.translatable("ftbquests.gui.edit").withStyle(ChatFormatting.ITALIC)).append(" >");
    }

    @Override
    public Color4I getColor(Map<Identifier, Boolean> value, Theme theme) {
        return theme.hasDarkBackground() ? EditableString.COLOR_HI : EditableString.COLOR_LO;
    }
}
