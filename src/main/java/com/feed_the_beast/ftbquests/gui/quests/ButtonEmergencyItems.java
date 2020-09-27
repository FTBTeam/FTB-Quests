package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.gui.GuiEmergencyItems;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author LatvianModder
 */
public class ButtonEmergencyItems extends ButtonTab
{
	public ButtonEmergencyItems(Panel panel)
	{
		super(panel, new TranslationTextComponent("ftbquests.file.emergency_items"), ThemeProperties.EMERGENCY_ITEMS_ICON.get());
	}

	@Override
	public void onClicked(MouseButton button)
	{
		playClickSound();
		new GuiEmergencyItems().openGui();
	}
}