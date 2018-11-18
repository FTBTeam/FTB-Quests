package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class MessageCreateObjectResponse extends MessageToClient
{
	private int id;
	private int parent;
	private QuestObjectBase object;
	private NBTTagCompound extra;

	public MessageCreateObjectResponse()
	{
	}

	public MessageCreateObjectResponse(QuestObjectBase o, @Nullable NBTTagCompound e)
	{
		id = o.uid;
		parent = o.getParentID();
		object = o;
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
		data.writeInt(id);
		data.writeNBT(extra);
		data.writeInt(parent);
		data.writeByte(object.getObjectType().ordinal());
		object.writeNetData(data);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
		extra = data.readNBT();
		parent = data.readInt();
		QuestObjectType type = QuestObjectType.VALUES[data.readUnsignedByte()];
		object = ClientQuestFile.INSTANCE.create(type, parent, extra == null ? new NBTTagCompound() : extra);
		object.readNetData(data);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		object.uid = id;

		if (object instanceof QuestObject)
		{
			((QuestObject) object).id = object.getCodeString();
		}

		object.onCreated();
		ClientQuestFile.INSTANCE.refreshIDMap();
		ClientQuestFile.INSTANCE.refreshGui();

		if (object instanceof Quest && ((Quest) object).chapter.quests.size() == 1) //Edge case, need to figure out better way
		{
			ClientQuestFile.INSTANCE.questTreeGui.resetScroll(true);
		}
	}
}