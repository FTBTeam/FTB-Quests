package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.gui.GuiEmergencyItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

/**
 * @author LatvianModder
 */
public class ButtonEmergencyItems extends ButtonTab
{
	public ButtonEmergencyItems(Panel panel)
	{
		super(panel, I18n.format("ftbquests.file.emergency_items"), ItemIcon.getItemIcon(new ItemStack(Blocks.ENDER_CHEST)));
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		new GuiEmergencyItems().openGui();
	}
}