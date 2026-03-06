package dev.ftb.mods.ftbquests.client.gui.quests;

import net.minecraft.client.gui.GuiGraphics;

import dev.ftb.mods.ftblibrary.client.gui.screens.KeyReferenceScreen;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;

public class QuestKeyReferenceScreen extends KeyReferenceScreen {
    public QuestKeyReferenceScreen(String... translationKeys) {
        super(translationKeys);
    }

    @Override
    protected void drawTextBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        IconHelper.renderIcon(ThemeProperties.KEY_REFERENCE_BACKGROUND.get(), graphics, x, y, w, h);
    }
}
