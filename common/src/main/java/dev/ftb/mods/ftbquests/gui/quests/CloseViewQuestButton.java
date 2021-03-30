package dev.ftb.mods.ftbquests.gui.quests;

import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.mojang.blaze3d.vertex.PoseStack;
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