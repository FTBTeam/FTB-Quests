package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestLink;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;

public class QuestLinkButton extends QuestButton {
    private final QuestLink link;

    public QuestLinkButton(QuestPanel questPanel, QuestLink link, Quest quest) {
        super(questPanel, quest);
        this.link = link;
    }

    @Override
    public Position getPosition() {
        return new Position(link.getX(), link.getY(), quest.size, quest.size);
    }

    @Override
    protected QuestObject theQuestObject() {
        return link;
    }

    @Override
    public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
        super.draw(matrixStack, theme, x, y, w, h);

        if (questScreen.file.canEdit()) {
            float s = w / 8F * 3F;
            matrixStack.pushPose();
            matrixStack.translate(x, y + h - s, 200);
            matrixStack.scale(s, s, 1F);
            ThemeProperties.LINK_ICON.get().draw(matrixStack, 0, 0, 1, 1);
            matrixStack.popPose();
        }
    }
}
