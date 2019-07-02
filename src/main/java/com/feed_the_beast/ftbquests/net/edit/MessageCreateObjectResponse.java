package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
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
	private QuestObjectType type;
	private NBTTagCompound nbt;
	private NBTTagCompound extra;

	public MessageCreateObjectResponse()
	{
	}

	public MessageCreateObjectResponse(QuestObjectBase o, @Nullable NBTTagCompound e)
	{
		id = o.id;
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
		data.writeInt(id);
		data.writeInt(parent);
		QuestObjectType.NAME_MAP.write(data, type);
		data.writeNBT(nbt);
		data.writeNBT(extra);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
		parent = data.readInt();
		type = QuestObjectType.NAME_MAP.read(data);
		nbt = data.readNBT();
		extra = data.readNBT();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		QuestObjectBase object = ClientQuestFile.INSTANCE.create(type, parent, extra == null ? new NBTTagCompound() : extra);
		object.readData(nbt);
		object.id = id;
		object.onCreated();
		ClientQuestFile.INSTANCE.refreshIDMap();
		object.editedFromGUI();
		FTBQuestsJEIHelper.refresh(object);
	}
}