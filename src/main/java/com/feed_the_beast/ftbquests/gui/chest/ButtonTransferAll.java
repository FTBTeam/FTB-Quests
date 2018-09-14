package com.feed_the_beast.ftbquests.gui.chest;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraftforge.items.SlotItemHandler;

/**
 * @author LatvianModder
 */
public class ButtonTransferAll extends Button
{
	public ButtonTransferAll(Panel panel)
	{
		super(panel, I18n.format("tile.ftbquests.chest.transfer_all"), Icon.EMPTY);
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		GuiQuestChest gui = (GuiQuestChest) getGui();

		for (Slot slot : gui.container.inventorySlots)
		{
			if (slot.getHasStack() && !(slot instanceof SlotItemHandler))
			{
				ClientUtils.MC.playerController.windowClick(gui.container.windowId, slot.slotNumber, 0, ClickType.QUICK_MOVE, ClientUtils.MC.player);
			}
		}
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		if (isMouseOver())
		{
			Color4I.WHITE.withAlpha(33).draw(x, y, w, h);
		}
	}
}