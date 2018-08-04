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
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public class MessageEditObject extends MessageToServer implements IConfigCallback
{
	private short id;

	public MessageEditObject()
	{
	}

	public MessageEditObject(short i)
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
		data.writeShort(id);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readShort();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuests.canEdit(player))
		{
			QuestObject object = ServerQuestFile.INSTANCE.get(id);

			if (object != null)
			{
				ITextComponent id = new TextComponentString(" #" + QuestFile.formatID(object.id));
				id.getStyle().setColor(TextFormatting.DARK_GRAY);
				id.getStyle().setBold(false);

				ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
				group.setDisplayName(new TextComponentTranslation(object.getObjectType().getTranslationKey()).appendSibling(id));
				ConfigGroup group1 = group.getGroup(object.getObjectType().getName());
				group1.setDisplayName(object.getDisplayName().appendSibling(StringUtils.color(new TextComponentString(" #" + object.id), TextFormatting.DARK_GRAY)));
				ConfigGroup g = group1;

				if (object instanceof QuestTask || object instanceof QuestReward)
				{
					g = group1.getGroup(((IStringSerializable) object).getName());

					if (object instanceof QuestTask)
					{
						group.setDisplayName(new TextComponentTranslation("ftbquests.task." + ((QuestTask) object).getName()).appendSibling(id));
					}
					else
					{
						group.setDisplayName(new TextComponentTranslation("ftbquests.reward." + ((QuestReward) object).getName()).appendSibling(id));
					}
				}

				object.getConfig(g);
				FTBLibAPI.editServerConfig(player, group, this);
			}
		}
	}

	@Override
	public void onConfigSaved(ConfigGroup g, ICommandSender sender)
	{
		QuestObject object = ServerQuestFile.INSTANCE.get(id);

		if (object != null)
		{
			ConfigGroup group = ConfigGroup.newGroup("object");
			object.getConfig(group);
			new MessageEditObjectResponse(id, group.serializeNBT()).sendToAll();
			ServerQuestFile.INSTANCE.save();
		}
	}
}