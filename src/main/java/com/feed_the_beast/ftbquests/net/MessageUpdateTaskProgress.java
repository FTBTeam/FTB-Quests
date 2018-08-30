package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.client.ClientQuestProgress;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class MessageUpdateTaskProgress extends MessageToClient
{
	private String team;
	private int task;
	private NBTBase nbt;

	public MessageUpdateTaskProgress()
	{
	}

	public MessageUpdateTaskProgress(String t, int k, @Nullable NBTBase d)
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
		data.writeString(team);
		data.writeShort(task);
		data.writeNBTBase(nbt);
	}

	@Override
	public void readData(DataIn data)
	{
		team = data.readString();
		task = data.readUnsignedShort();
		nbt = data.readNBTBase();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		QuestTask qtask = ClientQuestFile.INSTANCE.getTaskByIndex(task);

		if (qtask != null)
		{
			ClientQuestProgress data = team.isEmpty() ? ClientQuestFile.INSTANCE.self : ClientQuestFile.INSTANCE.getData(team);

			if (data != null)
			{
				data.getQuestTaskData(qtask).fromNBT(nbt);
			}
		}
	}
}