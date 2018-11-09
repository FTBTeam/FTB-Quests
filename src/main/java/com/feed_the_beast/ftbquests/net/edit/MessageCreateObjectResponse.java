package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageCreateObjectResponse extends MessageToClient
{
	private QuestObjectType type;
	private int id;
	private int parent;
	private NBTTagCompound nbt;

	public MessageCreateObjectResponse()
	{
	}

	public MessageCreateObjectResponse(QuestObjectType t, int i, int p, NBTTagCompound n)
	{
		type = t;
		id = i;
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
		data.writeInt(id);
		data.writeInt(parent);
		data.writeNBT(nbt);
	}

	@Override
	public void readData(DataIn data)
	{
		type = QuestObjectType.VALUES[data.readUnsignedByte()];
		id = data.readInt();
		parent = data.readInt();
		nbt = data.readNBT();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.INSTANCE != null)
		{
			QuestObject object = ClientQuestFile.INSTANCE.create(type, parent, nbt);

			if (object != null)
			{
				object.uid = id;
				object.id = object.getCodeString();
				object.readData(nbt);
				object.onCreated();
				ClientQuestFile.INSTANCE.refreshIDMap();
				ClientQuestFile.INSTANCE.refreshGui();
			}
		}
	}
}