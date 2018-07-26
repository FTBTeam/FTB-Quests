package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
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
		if (FTBQuests.canEdit(player))
		{
			QuestObject object = ServerQuestList.INSTANCE.get(id);

			if (object != null)
			{
				ConfigGroup group = new ConfigGroup(FTBQuests.MOD_ID);
				group.setDisplayName(new TextComponentString(FTBQuests.MOD_NAME));
				ConfigGroup group1 = group.getGroup(object.getObjectType().name().toLowerCase());
				group1.setDisplayName(object.getDisplayName().appendSibling(StringUtils.color(new TextComponentString(" #" + object.id), TextFormatting.DARK_GRAY)));
				ConfigGroup g = group1;

				if (object instanceof QuestTask || object instanceof QuestReward)
				{
					g = group1.getGroup(((IStringSerializable) object).getName());
				}

				object.getConfig(g);
				g.add("id", new ConfigInt(object.id), new ConfigInt(object.id)).setOrder((byte) -128).setCanEdit(false).setDisplayName(new TextComponentString("ID"));

				FTBLibAPI.editServerConfig(player, group, this);
			}
		}
	}

	@Override
	public void onConfigSaved(ConfigGroup g, ICommandSender sender)
	{
		QuestObject object = ServerQuestList.INSTANCE.get(id);

		if (object != null)
		{
			ConfigGroup group = new ConfigGroup("object");
			object.getConfig(group);
			new MessageEditObjectResponse(id, group.serializeNBT()).sendToAll();
			ServerQuestList.INSTANCE.save();
		}
	}
}