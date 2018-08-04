package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageUpdateTaskProgress extends MessageToClient
{
	private short task;
	private NBTTagCompound nbt;

	public MessageUpdateTaskProgress()
	{
	}

	public MessageUpdateTaskProgress(short k, NBTTagCompound d)
	{
		task = k;
		nbt = d;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeShort(task);
		data.writeNBT(nbt);
	}

	@Override
	public void readData(DataIn data)
	{
		task = data.readShort();
		nbt = data.readNBT();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		ClientQuestFile.INSTANCE.getQuestTaskData(task).readFromNBT(nbt);
	}
}