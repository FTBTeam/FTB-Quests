package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbguilibrary.utils.MouseButton;
import dev.ftb.mods.ftbguilibrary.widget.SimpleTextButton;
import dev.ftb.mods.ftbguilibrary.widget.Theme;
import dev.ftb.mods.ftbquests.net.MessageTogglePinned;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author LatvianModder
 */
public class PinViewQuestButton extends SimpleTextButton {
	public PinViewQuestButton(ViewQuestPanel parent) {
		super(parent, new TranslatableComponent(parent.questScreen.file.self.pinnedQuests.contains(parent.quest.id) ? "ftbquests.gui.unpin" : "ftbquests.gui.pin"), parent.questScreen.file.self.pinnedQuests.contains(parent.quest.id) ? ThemeProperties.PIN_ICON_ON.get() : ThemeProperties.PIN_ICON_OFF.get());
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		new MessageTogglePinned(((ViewQuestPanel) parent).quest.id).sendToServer();
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		drawIcon(matrixStack, theme, x + (w - 8) / 2, y + (h - 8) / 2, 8, 8);
	}
}