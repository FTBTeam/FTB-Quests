package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.permission.PermissionAPI;

/**
 * @author LatvianModder
 */
public class MessageEditObject extends MessageToServer implements IConfigCallback
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
				ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
				group.setDisplayName(new TextComponentString(FTBQuests.MOD_NAME));
				ConfigGroup group1 = group.getGroup(object.getObjectType().name().toLowerCase());
				group1.setDisplayName(object.getDisplayName().appendSibling(StringUtils.color(new TextComponentString(" #" + object.id), TextFormatting.DARK_GRAY)));

				if (object instanceof QuestTask || object instanceof QuestReward)
				{
					ConfigGroup group2 = group1.getGroup(((IStringSerializable) object).getName());
					object.getConfig(group2);
				}
				else
				{
					object.getConfig(group1);
				}

				FTBLibAPI.editServerConfig(player, group, this);
			}
		}
	}

	@Override
	public void onConfigSaved(ConfigGroup group, ICommandSender sender)
	{
		new MessageEditObjectResponse(id, group.serializeNBT()).sendToAll();
		ServerQuestList.INSTANCE.save();
	}
}