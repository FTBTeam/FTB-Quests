package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author LatvianModder
 */
public class CloseViewQuestButton extends SimpleTextButton {
	public CloseViewQuestButton(ViewQuestPanel parent) {
		super(parent, new TranslatableComponent("gui.close"), ThemeProperties.CLOSE_ICON.get(parent.quest));
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		((QuestScreen) getGui()).closeQuest();
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		drawIcon(matrixStack, theme, x + (w - 8) / 2, y + (h - 8) / 2, 8, 8);
	}
}