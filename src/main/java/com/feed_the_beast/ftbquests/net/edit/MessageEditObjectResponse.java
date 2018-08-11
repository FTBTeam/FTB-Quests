package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageEditObjectResponse extends MessageToClient
{
	private String prevId, id;
	private NBTTagCompound nbt;

	public MessageEditObjectResponse()
	{
	}

	public MessageEditObjectResponse(String pi, String i, NBTTagCompound n)
	{
		prevId = pi;
		id = i;
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
		data.writeString(prevId);
		data.writeString(id);
		data.writeNBT(nbt);
	}

	@Override
	public void readData(DataIn data)
	{
		prevId = data.readString();
		id = data.readString();
		nbt = data.readNBT();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.INSTANCE != null)
		{
			QuestObject object = ClientQuestFile.INSTANCE.get(prevId);

			if (object != null)
			{
				if (!prevId.equals(id))
				{
					object.id = id;
					ClientQuestFile.INSTANCE.map.remove(prevId);
					ClientQuestFile.INSTANCE.map.put(id, object);
				}

				ConfigGroup group = ConfigGroup.newGroup("object");
				object.getConfig(group);
				group.deserializeEditedNBT(nbt);
				object.clearCachedData();
				ClientQuestFile.INSTANCE.refreshGui(ClientQuestFile.INSTANCE);
			}
		}
	}
}