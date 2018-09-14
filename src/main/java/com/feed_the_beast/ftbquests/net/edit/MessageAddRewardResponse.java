package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestReward;
import net.minecraft.item.ItemStack;
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
	private boolean team;
	private ItemStack stack;

	public MessageAddRewardResponse()
	{
	}

	public MessageAddRewardResponse(String q, int i, boolean t, ItemStack is)
	{
		quest = q;
		uid = i;
		team = t;
		stack = is;
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
		data.writeBoolean(team);
		data.writeNBT(stack.isEmpty() ? null : stack.serializeNBT());
	}

	@Override
	public void readData(DataIn data)
	{
		quest = data.readString();
		uid = data.readInt();
		team = data.readBoolean();
		NBTTagCompound nbt = data.readNBT();
		stack = nbt == null ? ItemStack.EMPTY : new ItemStack(nbt);
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
				QuestReward r = new QuestReward(q, uid);
				r.team = team;
				r.stack = stack;
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