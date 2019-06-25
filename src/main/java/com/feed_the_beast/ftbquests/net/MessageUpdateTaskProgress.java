package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestData;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class MessageUpdateTaskProgress extends MessageToClient
{
	private short team;
	private int task;
	private NBTBase nbt;

	public MessageUpdateTaskProgress()
	{
	}

	public MessageUpdateTaskProgress(short t, int k, @Nullable NBTBase d)
	{
		team = t;
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
		data.writeShort(team);
		data.writeInt(task);
		data.writeNBTBase(nbt);
	}

	@Override
	public void readData(DataIn data)
	{
		team = data.readShort();
		task = data.readInt();
		nbt = data.readNBTBase();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		QuestTask qtask = ClientQuestFile.INSTANCE.getTask(task);

		if (qtask != null)
		{
			ClientQuestData data = ClientQuestFile.INSTANCE.getData(team);

			if (data != null)
			{
				ClientQuestFile.INSTANCE.clearCachedProgress(team);
				data.getQuestTaskData(qtask).fromNBT(nbt);
			}
		}
	}
}