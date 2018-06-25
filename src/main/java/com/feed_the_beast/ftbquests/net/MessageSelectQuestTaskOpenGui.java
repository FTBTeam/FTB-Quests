package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.GuiSelectQuestTask;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageSelectQuestTaskOpenGui extends MessageToClient
{
	private BlockPos pos;

	public MessageSelectQuestTaskOpenGui()
	{
	}

	public MessageSelectQuestTaskOpenGui(BlockPos p)
	{
		pos = p;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writePos(pos);
	}

	@Override
	public void readData(DataIn data)
	{
		pos = data.readPos();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		new GuiSelectQuestTask(pos).openGui();
	}
}