package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class MessageEditRewardResponse extends MessageToClient
{
	private int id;
	private NBTTagCompound nbt;

	public MessageEditRewardResponse()
	{
	}

	public MessageEditRewardResponse(int i, @Nullable NBTTagCompound n)
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
		data.writeInt(id);
		data.writeNBT(nbt);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
		nbt = data.readNBT();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.INSTANCE != null)
		{
			QuestReward reward = ClientQuestFile.INSTANCE.getReward(id);

			if (reward != null)
			{
				ClientQuestFile.INSTANCE.clearCachedData();
				ConfigGroup group = ConfigGroup.newGroup("reward");
				reward.getConfig(group);
				reward.getExtraConfig(group);
				group.deserializeEditedNBT(nbt);

				GuiQuestTree gui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

				if (gui != null)
				{
					gui.questRight.refreshWidgets();
				}
			}
		}
	}
}