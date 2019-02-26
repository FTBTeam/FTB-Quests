package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;

/**
 * @author LatvianModder
 */
public class ButtonOpenInGuide extends SimpleTextButton
{
	private final Quest quest;

	public ButtonOpenInGuide(Panel panel, Quest q)
	{
		super(panel, I18n.format("ftbquests.gui.open_in_guide"), ItemIcon.getItemIcon(Items.BOOK));
		quest = q;
	}

	@Override
	public void onClicked(MouseButton button)
	{
		handleClick("guide", quest.guidePage);
	}
}