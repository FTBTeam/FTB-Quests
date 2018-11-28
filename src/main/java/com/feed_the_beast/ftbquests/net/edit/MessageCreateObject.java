package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class MessageCreateObject extends MessageToServer
{
	private int parent;
	private QuestObjectType type;
	private NBTTagCompound nbt;
	private NBTTagCompound extra;

	public MessageCreateObject()
	{
	}

	public MessageCreateObject(QuestObjectBase o, @Nullable NBTTagCompound e)
	{
		parent = o.getParentID();
		type = o.getObjectType();
		nbt = new NBTTagCompound();
		o.writeData(nbt);
		extra = e;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeInt(parent);
		data.writeByte(type.ordinal());
		data.writeNBT(nbt);
		data.writeNBT(extra);
	}

	@Override
	public void readData(DataIn data)
	{
		parent = data.readInt();
		type = QuestObjectType.ALL.get(data.readUnsignedByte());
		nbt = data.readNBT();
		extra = data.readNBT();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuests.canEdit(player))
		{
			QuestObjectBase object = ServerQuestFile.INSTANCE.create(type, parent, extra == null ? new NBTTagCompound() : extra);
			object.readData(nbt);
			object.uid = ServerQuestFile.INSTANCE.readID(0);
			object.onCreated();
			ServerQuestFile.INSTANCE.refreshIDMap();
			ServerQuestFile.INSTANCE.clearCachedData();
			ServerQuestFile.INSTANCE.save();
			new MessageCreateObjectResponse(object, extra).sendToAll();
		}
	}
}