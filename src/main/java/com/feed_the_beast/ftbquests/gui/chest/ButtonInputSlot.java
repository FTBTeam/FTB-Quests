package com.feed_the_beast.ftbquests.gui.chest;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonInputSlot extends Button
{
	public ButtonInputSlot(Panel panel)
	{
		super(panel);
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiQuestChest gui = (GuiQuestChest) getGui();

		if (gui.container.enchantItem(Minecraft.getMinecraft().player, 0))
		{
			Minecraft.getMinecraft().playerController.sendEnchantPacket(gui.container.windowId, 0);
		}
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		list.add(TextFormatting.GRAY + I18n.format("tile.ftbquests.chest.input"));
		list.add(TextFormatting.DARK_GRAY + I18n.format("tile.ftbquests.chest.input_desc"));
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		if (isMouseOver())
		{
			Color4I.WHITE.withAlpha(150).draw(x, y, w, h);
		}
	}
}