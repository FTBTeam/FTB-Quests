package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class MessageUpdateTaskProgress extends MessageToClient
{
	private short task;
	private NBTBase nbt;

	public MessageUpdateTaskProgress()
	{
	}

	public MessageUpdateTaskProgress(short k, @Nullable NBTBase d)
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

		if (nbt == null)
		{
			data.writeByte(0);
		}
		else if (nbt instanceof NBTTagCompound)
		{
			data.writeByte(1);
			data.writeNBT((NBTTagCompound) nbt);
		}
		else
		{
			data.writeByte(2);
			NBTTagCompound nbt1 = new NBTTagCompound();
			nbt1.setTag("_", nbt);
			data.writeNBT(nbt1);
		}
	}

	@Override
	public void readData(DataIn data)
	{
		task = data.readShort();

		int i = data.readByte();

		if (i == 0)
		{
			nbt = null;
		}
		else if (i == 1)
		{
			nbt = data.readNBT();
		}
		else
		{
			nbt = data.readNBT().getTag("_");
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		QuestTask qtask = ClientQuestFile.INSTANCE.getTask(task);

		if (qtask != null)
		{
			ClientQuestFile.INSTANCE.getQuestTaskData(qtask).fromNBT(nbt);
		}
	}
}