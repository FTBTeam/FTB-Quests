package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestLink;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.gui.GuiGraphics;

public class QuestLinkButton extends QuestButton {
    private final QuestLink link;

    public QuestLinkButton(QuestPanel questPanel, QuestLink link, Quest quest) {
        super(questPanel, quest);
        this.link = link;
    }

    @Override
    public Position getPosition() {
        return new Position(link.getX(), link.getY(), link.getWidth(), link.getHeight());
    }

    @Override
    protected QuestObject theQuestObject() {
        return link;
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        super.draw(graphics, theme, x, y, w, h);

        if (questScreen.file.canEdit()) {
            float s = w / 8F * 3F;
            graphics.pose().pushMatrix();
            graphics.pose().translate(x, y + h - s);
            graphics.pose().scale(s, s);
            IconHelper.renderIcon(ThemeProperties.LINK_ICON.get(), graphics, 0, 0, 1, 1);
            graphics.pose().popMatrix();
        }
    }

    @Override
    protected String getShape() {
        return link.getShape();
    }
}
