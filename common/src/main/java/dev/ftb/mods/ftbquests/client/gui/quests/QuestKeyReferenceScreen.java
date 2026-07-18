package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.misc.KeyReferenceScreen;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.client.FTBQuestsKeyMappings;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class QuestKeyReferenceScreen extends KeyReferenceScreen {
    private static final Style HEADER_STYLE = Style.EMPTY.withBold(true).withUnderlined(true).withColor(ChatFormatting.YELLOW);
    private static final Style DESC_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY);
    private static final Pair<Component, Component> EMPTY_PAIR = Pair.of(Component.empty(), Component.empty());

    public QuestKeyReferenceScreen(boolean canEdit) {
        super(buildComponents(canEdit));
    }

    private static List<Pair<Component, Component>> buildComponents(boolean canEdit) {
        List<Pair<Component, Component>> components = new ArrayList<>();

        components.add(title("ftbquests.gui.kr.general_controls"));
        components.addAll(componentsFromBindings(FTBQuestsKeyMappings.GUI_KEYS));
        components.add(componentsFromSplitKey("ftbquests.gui.kr.view_quest_details"));
        components.add(componentsFromSplitKey("ftbquests.gui.kr.scroll_up_down"));
        components.add(componentsFromSplitKey("ftbquests.gui.kr.scroll_left_right"));
        components.add(componentsFromSplitKey("ftbquests.gui.kr.zoom_in_out"));
        components.add(componentsFromSplitKey("ftbquests.gui.kr.go_back"));
        components.add(getKeyBindingInfo(FTBQuestsKeyMappings.KEY_GUI_PLAYER_PREFS));

        if (!canEdit) {
            return components;
        }

        components.add(EMPTY_PAIR);
        components.add(title("ftbquests.gui.kr.editor_mode"));
        components.add(componentsFromSplitKey("ftbquests.gui.kr.toggle_selection"));
        components.add(componentsFromSplitKey("ftbquests.gui.kr.properties_editor"));
        components.add(componentsFromSplitKey("ftbquests.gui.kr.copy_quest"));
        components.add(componentsFromSplitKey("ftbquests.gui.kr.move_selected_quests"));
        components.add(componentsFromSplitKey("ftbquests.gui.kr.context_menu"));
        components.add(componentsFromSplitKey("ftbquests.gui.kr.drag_highlight"));
        components.add(componentsFromSplitKey("ftbquests.gui.kr.drag_toggle_select"));
        components.addAll(componentsFromBindings(FTBQuestsKeyMappings.EDITOR_KEYS));
        components.add(componentsFromSplitKey("ftbquests.gui.kr.paste_no_deps"));
        components.add(componentsFromSplitKey("ftbquests.gui.kr.paste_as_link"));
        components.add(getKeyBindingInfo(FTBQuestsKeyMappings.KEY_GUI_RELOAD_THEME));
        components.add(componentsFromSplitKey("ftbquests.gui.kr.reload_no_toast"));

        components.add(EMPTY_PAIR);
        components.add(title("ftbquests.gui.kr.quest_view_panel"));
        components.addAll(componentsFromBindings(FTBQuestsKeyMappings.QUEST_PANEL_KEYS));

        return components;
    }

    private static Pair<Component, Component> title(String i18nKey) {
        return Pair.of(Component.translatable(i18nKey).copy().withStyle(HEADER_STYLE), Component.empty());
    }

    private static List<Pair<Component, Component>> componentsFromBindings(List<KeyMapping> keys) {
        List<Pair<Component, Component>> components = new ArrayList<>();
        for (KeyMapping key : keys) {
            components.add(getKeyBindingInfo(key));
        }
        return components;
    }

    private static Pair<Component, Component> getKeyBindingInfo(KeyMapping key) {
        Component bindingKey = key.getTranslatedKeyMessage();
        Component translation = Component.translatable(key.getName());
        return Pair.of(bindingKey, translation.copy().withStyle(DESC_STYLE));
    }

    private static Pair<Component, Component> componentsFromSplitKey(String i18nKey) {
        String[] parts = I18n.get(i18nKey).split(";");
        Component left = substituteKeyMapping(parts[0]);
        Component right = parts.length > 1 ? Component.literal(parts[1]) : Component.empty();
        return Pair.of(left, right.copy().withStyle(DESC_STYLE));
    }

    private static Component substituteKeyMapping(String input) {
        int s = input.indexOf('{');
        int e = input.indexOf('}');
        if (s >= 0 && e >= 0 && e > s + 1) {
            String key = input.substring(s + 1, e);
            var keyMapping = KeyMapping.ALL.get(key);
            if (keyMapping != null) {
                return Component.literal(input.substring(0, s))
                        .append(ClientUtils.input().getKeyMappingDisplayName(keyMapping))
                        .append(input.substring(e + 1));
            }
        }

        return Component.literal(input);
    }

    @Override
    protected void drawTextBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        ThemeProperties.KEY_REFERENCE_BACKGROUND.get().draw(graphics, x, y, w, h);
    }
}
