package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
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
	private int uid;
	private NBTTagCompound nbt;

	public MessageEditRewardResponse()
	{
	}

	public MessageEditRewardResponse(int id, @Nullable NBTTagCompound n)
	{
		uid = id;
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
		data.writeInt(uid);
		data.writeNBT(nbt);
	}

	@Override
	public void readData(DataIn data)
	{
		uid = data.readInt();
		nbt = data.readNBT();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.INSTANCE != null)
		{
			QuestReward r = ClientQuestFile.INSTANCE.allRewards.get(uid);

			if (r != null)
			{
				if (nbt == null)
				{
					r.quest.rewards.remove(r);
					ClientQuestFile.INSTANCE.allRewards.remove(r.uid);
				}
				else
				{
					r.readCommonData(nbt);
				}

				GuiQuestTree gui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

				if (gui != null)
				{
					gui.questRight.refreshWidgets();
				}
			}
		}
	}
}