package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.misc.KeyReferenceScreen;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.gui.GuiGraphics;

public class QuestKeyReferenceScreen extends KeyReferenceScreen {
    public QuestKeyReferenceScreen(String... translationKeys) {
        super(translationKeys);
    }

    @Override
    protected void drawTextBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        IconHelper.renderIcon(ThemeProperties.KEY_REFERENCE_BACKGROUND.get(), graphics, x, y, w, h);
    }
}
