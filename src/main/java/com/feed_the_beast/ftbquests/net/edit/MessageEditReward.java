package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

/**
 * @author LatvianModder
 */
public class MessageEditReward extends MessageToServer
{
	private String quest;
	private int index;
	private boolean team;
	private ItemStack stack;

	public MessageEditReward()
	{
	}

	public MessageEditReward(String q, int i, boolean t, ItemStack is)
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
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuests.canEdit(player))
		{
			Quest q = ServerQuestFile.INSTANCE.getQuest(quest);

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

				ServerQuestFile.INSTANCE.save();
				new MessageEditRewardResponse(quest, index, team, stack).sendToAll();
			}
		}
	}
}