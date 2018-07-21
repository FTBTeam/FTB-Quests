package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.server.permission.PermissionAPI;

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
		if (PermissionAPI.hasPermission(player, FTBQuests.PERM_EDIT))
		{
			QuestObject object = ServerQuestList.INSTANCE.createAndAdd(type, parent, nbt);

			if (object != null)
			{
				object.writeData(nbt);
				new MessageCreateObjectResponse(type, parent, nbt).sendToAll();
				ServerQuestList.INSTANCE.save();
			}
		}
	}
}