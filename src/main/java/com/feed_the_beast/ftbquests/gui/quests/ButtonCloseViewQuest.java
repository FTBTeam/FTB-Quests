package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author LatvianModder
 */
public class ButtonCloseViewQuest extends SimpleTextButton
{
	public ButtonCloseViewQuest(PanelViewQuest parent)
	{
		super(parent, new TranslationTextComponent("gui.close"), ThemeProperties.CLOSE_ICON.get(parent.quest));
	}

	@Override
	public void onClicked(MouseButton button)
	{
		playClickSound();
		((GuiQuests) getGui()).closeQuest();
	}

	@Override
	public void drawBackground(MatrixStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
	}
}