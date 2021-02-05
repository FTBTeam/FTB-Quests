package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.gui.ComponentTextBox;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;

/**
 * @author LatvianModder
 */
public class TextFieldDisabledButton extends ComponentTextBox
{
	public TextFieldDisabledButton(Panel panel, Component text)
	{
		super(panel);
		addFlags(Theme.CENTERED | Theme.CENTERED_V);
		setText(text);
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
	}
}