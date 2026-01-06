package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import dev.ftb.mods.ftbquests.quest.QuestShape;
import net.minecraft.client.gui.GuiGraphics;

public enum QuestShapeRenderer implements IconRenderer<QuestShape> {
    INSTANCE;

    @Override
    public void render(QuestShape icon, GuiGraphics graphics, int x, int y, int w, int h) {
        IconHelper.renderIcon(icon.getBackground(), graphics, x, y, w, h);
        IconHelper.renderIcon(icon.getOutline(), graphics, x, y, w, h);
    }
}
