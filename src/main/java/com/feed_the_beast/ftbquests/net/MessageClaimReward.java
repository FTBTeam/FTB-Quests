package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageClaimReward extends MessageBase
{
	private final int id;
	private final boolean notify;

	MessageClaimReward(PacketBuffer buffer)
	{
		id = buffer.readVarInt();
		notify = buffer.readBoolean();
	}

	public MessageClaimReward(int i, boolean n)
	{
		id = i;
		notify = n;
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		buffer.writeVarInt(id);
		buffer.writeBoolean(notify);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		Reward reward = ServerQuestFile.INSTANCE.getReward(id);
		ServerPlayerEntity player = context.getSender();

		if (reward != null)
		{
			PlayerData teamData = ServerQuestFile.INSTANCE.getData(player);

			if (teamData.isComplete(reward.quest))
			{
				teamData.claimReward(player, reward, notify);
			}
		}
	}
}