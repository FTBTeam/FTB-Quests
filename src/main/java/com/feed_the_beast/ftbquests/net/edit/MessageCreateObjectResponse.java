package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageCreateObjectResponse extends MessageToClient
{
	private QuestObjectType type;
	private String parent;
	private NBTTagCompound nbt;

	public MessageCreateObjectResponse()
	{
	}

	public MessageCreateObjectResponse(QuestObjectType t, String p, NBTTagCompound n)
	{
		type = t;
		parent = p;
		nbt = n;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeByte(type.ordinal());
		data.writeString(parent);
		data.writeNBT(nbt);
	}

	@Override
	public void readData(DataIn data)
	{
		type = QuestObjectType.VALUES[data.readUnsignedByte()];
		parent = data.readString();
		nbt = data.readNBT();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.INSTANCE != null)
		{
			QuestObject object = ClientQuestFile.INSTANCE.createAndAdd(type, parent, nbt);

			if (object != null)
			{
				ClientQuestFile.INSTANCE.refreshIDMap();

				if (object instanceof QuestTask)
				{
					ClientQuestFile.INSTANCE.refreshTaskList();

					for (IProgressData data : ClientQuestFile.INSTANCE.getAllData())
					{
						data.createTaskData((QuestTask) object);
					}
				}

				ClientQuestFile.INSTANCE.refreshGui(ClientQuestFile.INSTANCE);
			}
		}
	}
}