package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigValueInstance;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiSelectItemStack;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.gui.QuestsTheme;
import com.feed_the_beast.ftbquests.net.edit.MessageAddReward;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * @author LatvianModder
 */
public class ButtonAddReward extends SimpleTextButton
{
	private final Quest quest;

	public ButtonAddReward(Panel panel, Quest q)
	{
		super(panel, I18n.format("gui.add"), QuestsTheme.ADD);
		quest = q;
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();

		ConfigValueInstance value = new ConfigValueInstance("item", ConfigGroup.DEFAULT, new ConfigItemStack(new ItemStack(Items.APPLE))
		{
			@Override
			public void setStack(ItemStack stack)
			{
				new MessageAddReward(quest.getID(), false, stack).sendToServer();
			}
		});

		new GuiSelectItemStack(value, this).openGui();
	}
}