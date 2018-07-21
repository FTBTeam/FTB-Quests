package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.server.permission.PermissionAPI;

/**
 * @author LatvianModder
 */
public class MessageEditObject extends MessageToServer
{
	private int id;

	public MessageEditObject()
	{
	}

	public MessageEditObject(int i)
	{
		id = i;
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
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (id != 0 && PermissionAPI.hasPermission(player, FTBQuests.PERM_EDIT))
		{
			QuestObject object = ServerQuestList.INSTANCE.get(id);

			if (object != null)
			{
				ConfigGroup group = new ConfigGroup(object.getDisplayName().appendText("#" + object.id));
				object.getConfig(group);
				FTBLibAPI.editServerConfig(player, group, (g, sender, json) -> {
					g.fromJson(json);
					new MessageEditObjectResponse(id, json).sendToAll();
					ServerQuestList.INSTANCE.save();
				});
			}
		}
	}
}