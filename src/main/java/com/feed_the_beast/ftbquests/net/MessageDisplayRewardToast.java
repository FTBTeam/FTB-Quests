package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.IRewardListenerGui;
import com.feed_the_beast.ftbquests.gui.RewardKey;
import com.feed_the_beast.ftbquests.gui.RewardToast;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageDisplayRewardToast extends MessageToClient
{
	private int id;
	private ITextComponent text;
	private Icon icon;

	public MessageDisplayRewardToast()
	{
	}

	public MessageDisplayRewardToast(int _id, ITextComponent t, Icon i)
	{
		id = _id;
		text = t;
		icon = i;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeInt(id);
		data.writeTextComponent(text);
		data.writeIcon(icon);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
		text = data.readTextComponent();
		icon = data.readIcon();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		Icon i = icon.isEmpty() ? ClientQuestFile.INSTANCE.getBase(id).getIcon() : icon;

		if (!IRewardListenerGui.add(new RewardKey(text.getUnformattedText(), i), 1))
		{
			Minecraft.getMinecraft().getToastGui().add(new RewardToast(text.getFormattedText(), i));
		}
	}
}