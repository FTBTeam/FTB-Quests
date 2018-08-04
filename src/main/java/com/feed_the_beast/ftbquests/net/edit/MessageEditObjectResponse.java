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
	private short id;
	private NBTTagCompound nbt;

	public MessageEditObjectResponse()
	{
	}

	public MessageEditObjectResponse(short i, NBTTagCompound n)
	{
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
		data.writeShort(id);
		data.writeNBT(nbt);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readShort();
		nbt = data.readNBT();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.INSTANCE != null)
		{
			QuestObject object = ClientQuestFile.INSTANCE.get(id);

			if (object != null)
			{
				ConfigGroup group = ConfigGroup.newGroup("object");
				object.getConfig(group);
				group.deserializeNBT(nbt);
				ClientQuestFile.INSTANCE.refreshGui(ClientQuestFile.INSTANCE);
			}
		}
	}
}