package dev.ftb.mods.ftbquests.client.config;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.gui.TeamRewardByTypeEditorScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class EditableTeamRewardByType extends ConfigValue<Map<ResourceLocation, Boolean>> {
    @Override
    public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
        var gui = new TeamRewardByTypeEditorScreen(this, callback);
        gui.setTitle(Component.translatable("ftbquests.file.defaults.reward_team_by_type"));
        gui.openGui();
    }

    @Override
    public Component getStringForGUI(Map<ResourceLocation, Boolean> value) {
        return Component.literal("< ").append(Component.translatable("ftbquests.gui.edit").withStyle(ChatFormatting.ITALIC)).append(" >");
    }

    @Override
    public Color4I getColor(Map<ResourceLocation, Boolean> value) {
        return StringConfig.COLOR;
    }
}
