package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.ChoiceReward;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageClaimChoiceReward extends MessageBase
{
	private final int id;
	private final int index;

	public MessageClaimChoiceReward(int i, int idx)
	{
		id = i;
		index = idx;
	}

	MessageClaimChoiceReward(PacketBuffer buffer)
	{
		id = buffer.readVarInt();
		index = buffer.readVarInt();
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		buffer.writeVarInt(id);
		buffer.writeVarInt(index);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		Reward reward = ServerQuestFile.INSTANCE.getReward(id);

		if (reward instanceof ChoiceReward)
		{
			ServerPlayerEntity player = context.getSender();
			ChoiceReward r = (ChoiceReward) reward;
			PlayerData data = PlayerData.get(player);

			if (r.getTable() != null && data.isComplete(reward.quest))
			{
				if (index >= 0 && index < r.getTable().rewards.size())
				{
					r.getTable().rewards.get(index).reward.claim(player, true);
					data.claimReward(player, reward, true);
				}
			}
		}
	}
}