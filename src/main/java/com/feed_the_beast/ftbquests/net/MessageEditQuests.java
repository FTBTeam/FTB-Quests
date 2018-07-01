package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.edit.GuiEditQuests;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageEditQuests extends MessageToClient
{
	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public boolean hasData()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		new GuiEditQuests().openGui();
	}
}