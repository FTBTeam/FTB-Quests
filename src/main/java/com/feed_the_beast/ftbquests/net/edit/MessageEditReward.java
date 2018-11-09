package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageEditReward extends MessageToServer implements IConfigCallback
{
	private int id;

	public MessageEditReward()
	{
	}

	public MessageEditReward(int i)
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
			QuestReward reward = ServerQuestFile.INSTANCE.getReward(id);

			if (reward != null)
			{
				QuestRewardType type = reward.getType();
				ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
				group.setDisplayName(type.getDisplayName());
				ConfigGroup g = group.getGroup("reward").getGroup(type.getRegistryName().getNamespace()).getGroup(type.getRegistryName().getPath());
				reward.getConfig(g);
				reward.getExtraConfig(g);
				FTBLibAPI.editServerConfig(player, group, this);
			}
		}
	}

	@Override
	public void onConfigSaved(ConfigGroup g, ICommandSender sender)
	{
		QuestReward reward = ServerQuestFile.INSTANCE.getReward(id);

		if (reward != null)
		{
			ServerQuestFile.INSTANCE.clearCachedData();
			ConfigGroup group = ConfigGroup.newGroup("reward");
			reward.getConfig(group);
			reward.getExtraConfig(group);
			new MessageEditRewardResponse(id, group.serializeNBT()).sendToAll();
			ServerQuestFile.INSTANCE.save();
		}
	}
}