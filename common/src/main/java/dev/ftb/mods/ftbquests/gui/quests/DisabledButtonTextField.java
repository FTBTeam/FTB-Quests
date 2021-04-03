package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbguilibrary.widget.ComponentTextField;
import dev.ftb.mods.ftbguilibrary.widget.Panel;
import dev.ftb.mods.ftbguilibrary.widget.Theme;
import net.minecraft.network.chat.Component;

/**
 * @author LatvianModder
 */
public class DisabledButtonTextField extends ComponentTextField {
	public DisabledButtonTextField(Panel panel, Component text) {
		super(panel);
		addFlags(Theme.CENTERED | Theme.CENTERED_V);
		setText(text);
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
	}
}