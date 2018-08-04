package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageUpdateRewardStatus extends MessageToClient
{
	private short reward;
	private boolean status;

	public MessageUpdateRewardStatus()
	{
	}

	public MessageUpdateRewardStatus(short r, boolean s)
	{
		reward = r;
		status = s;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeShort(reward);
		data.writeBoolean(status);
	}

	@Override
	public void readData(DataIn data)
	{
		reward = data.readShort();
		status = data.readBoolean();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		ClientQuestFile.INSTANCE.setRewardStatus(reward, status);
	}
}