package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.quest.QuestReward;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageEditRewardResponse extends MessageToClient
{
	private int uid;
	private boolean team;
	private ItemStack stack;

	public MessageEditRewardResponse()
	{
	}

	public MessageEditRewardResponse(int i, boolean t, ItemStack is)
	{
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
		data.writeInt(uid);
		data.writeBoolean(team);
		data.writeNBT(stack.isEmpty() ? null : stack.serializeNBT());
	}

	@Override
	public void readData(DataIn data)
	{
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
			QuestReward q = ClientQuestFile.INSTANCE.allRewards.get(uid);

			if (q != null)
			{
				q.team = team;
				q.stack = stack;

				if (q.stack.isEmpty())
				{
					q.quest.rewards.remove(q);
					ClientQuestFile.INSTANCE.allRewards.remove(q.uid);
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