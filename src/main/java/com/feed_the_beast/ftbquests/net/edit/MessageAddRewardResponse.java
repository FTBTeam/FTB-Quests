package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageAddRewardResponse extends MessageToClient
{
	private String quest;
	private int uid;
	private NBTTagCompound nbt;

	public MessageAddRewardResponse()
	{
	}

	public MessageAddRewardResponse(String q, int i, NBTTagCompound n)
	{
		quest = q;
		uid = i;
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
		data.writeString(quest);
		data.writeInt(uid);
		data.writeNBT(nbt);
	}

	@Override
	public void readData(DataIn data)
	{
		quest = data.readString();
		uid = data.readInt();
		nbt = data.readNBT();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.INSTANCE != null)
		{
			Quest q = ClientQuestFile.INSTANCE.getQuest(quest);

			if (q != null)
			{
				QuestReward r = QuestRewardType.createReward(q, nbt);

				if (r != null)
				{
					r.uid = uid;
					q.rewards.add(r);
					ClientQuestFile.INSTANCE.allRewards.put(r.uid, r);

					GuiQuestTree gui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

					if (gui != null)
					{
						gui.questRight.refreshWidgets();
					}
				}
			}
		}
	}
}