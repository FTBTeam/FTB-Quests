package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author LatvianModder
 */
public class MessageCreateObject extends MessageToServer
{
	private QuestObjectType type;
	private int parent;
	private NBTTagCompound nbt;

	public MessageCreateObject()
	{
	}

	public MessageCreateObject(QuestObjectType t, int p, NBTTagCompound n)
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
		data.writeInt(parent);
		data.writeNBT(nbt);
	}

	@Override
	public void readData(DataIn data)
	{
		type = QuestObjectType.VALUES[data.readUnsignedByte()];
		parent = data.readInt();
		nbt = data.readNBT();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuests.canEdit(player))
		{
			QuestObject object = ServerQuestFile.INSTANCE.create(type, parent, nbt);

			if (object != null)
			{
				object.readData(nbt);
				object.uid = ServerQuestFile.INSTANCE.readID(0);
				object.id = object.getCodeString();
				object.onCreated();
				ServerQuestFile.INSTANCE.refreshIDMap();
				ServerQuestFile.INSTANCE.clearCachedData();
				NBTTagCompound nbt1 = new NBTTagCompound();
				object.writeData(nbt1);
				new MessageCreateObjectResponse(type, object.uid, parent, nbt1).sendToAll();
				ServerQuestFile.INSTANCE.save();
			}
		}
	}
}