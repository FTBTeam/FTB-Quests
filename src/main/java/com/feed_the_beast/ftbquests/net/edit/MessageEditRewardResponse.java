package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.GuiQuest;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * @author LatvianModder
 */
public class MessageEditRewardResponse extends MessageToClient
{
	private String quest;
	private int index;
	private boolean team;
	private ItemStack stack;

	public MessageEditRewardResponse()
	{
	}

	public MessageEditRewardResponse(String q, int i, boolean t, ItemStack is)
	{
		quest = q;
		index = i;
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
		data.writeShort(index);
		data.writeBoolean(team);
		data.writeNBT(stack.isEmpty() ? null : stack.serializeNBT());
	}

	@Override
	public void readData(DataIn data)
	{
		quest = data.readString();
		index = data.readUnsignedShort();
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
				List<ItemStack> list = team ? q.teamRewards : q.playerRewards;

				if (index >= list.size())
				{
					if (!stack.isEmpty())
					{
						list.add(stack);
					}
				}
				else if (stack.isEmpty())
				{
					list.remove(index);
				}
				else
				{
					list.set(index, stack);
				}

				GuiQuest gui = ClientUtils.getCurrentGuiAs(GuiQuest.class);

				if (gui != null)
				{
					gui.rewards.refreshWidgets();
				}
			}
		}
	}
}