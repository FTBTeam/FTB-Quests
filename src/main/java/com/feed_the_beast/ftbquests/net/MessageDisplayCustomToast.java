package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.ToastCustom;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageDisplayCustomToast extends MessageToClient
{
	private ITextComponent text;
	private Icon icon;
	private String description;

	public MessageDisplayCustomToast()
	{
	}

	public MessageDisplayCustomToast(ITextComponent t, Icon i, String d)
	{
		text = t;
		icon = i;
		description = d;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeTextComponent(text);
		data.writeIcon(icon);
		data.writeString(description);
	}

	@Override
	public void readData(DataIn data)
	{
		text = data.readTextComponent();
		icon = data.readIcon();
		description = data.readString();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		Minecraft.getMinecraft().getToastGui().add(new ToastCustom(text.getFormattedText(), icon, GuiQuestTree.fixI18n(null, description)));
	}
}