package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
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
			QuestObjectBase object = ServerQuestFile.INSTANCE.getBase(id);

			if (object != null)
			{
				ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);

				if (object.getObjectType() != QuestObjectType.FILE)
				{
					ITextComponent idc = new TextComponentString(" " + object);
					idc.getStyle().setColor(TextFormatting.DARK_GRAY);
					idc.getStyle().setBold(false);
					group.setDisplayName(new TextComponentTranslation(object.getObjectType().getTranslationKey()).appendSibling(idc));
				}
				else
				{
					group.setDisplayName(new TextComponentTranslation(object.getObjectType().getTranslationKey()));
				}

				ConfigGroup g = object.createSubGroup(group);
				//g.setDisplayName(object.getDisplayName().createCopy().appendSibling(StringUtils.color(new TextComponentString(" " + object), TextFormatting.DARK_GRAY)));
				object.getConfig(g);
				FTBLibAPI.editServerConfig(player, group, this);
			}
		}
	}

	@Override
	public void onConfigSaved(ConfigGroup g, ICommandSender sender)
	{
		QuestObjectBase object = ServerQuestFile.INSTANCE.getBase(id);

		if (object != null)
		{
			ServerQuestFile.INSTANCE.clearCachedData();
			ConfigGroup group = ConfigGroup.newGroup("object");
			object.getConfig(group);
			new MessageEditObjectResponse(object).sendToAll();
			ServerQuestFile.INSTANCE.save();
		}
	}
}