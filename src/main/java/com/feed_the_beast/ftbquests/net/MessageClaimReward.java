package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageClaimReward extends MessageBase
{
	private final int id;
	private final boolean notify;

	MessageClaimReward(FriendlyByteBuf buffer)
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
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeVarInt(id);
		buffer.writeBoolean(notify);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		Reward reward = ServerQuestFile.INSTANCE.getReward(id);
		ServerPlayer player = context.getSender();

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