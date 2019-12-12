package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageClaimAllRewards extends MessageBase
{
	public MessageClaimAllRewards()
	{
	}

	MessageClaimAllRewards(PacketBuffer buffer)
	{
	}

	public void write(PacketBuffer buffer)
	{
	}

	public void handle(NetworkEvent.Context context)
	{
		ServerPlayerEntity player = context.getSender();
		PlayerData data = PlayerData.get(player);

		for (Chapter chapter : ServerQuestFile.INSTANCE.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				if (data.isComplete(quest))
				{
					for (Reward reward : quest.rewards)
					{
						if (!reward.getExcludeFromClaimAll())
						{
							data.claimReward(player, reward, true);
						}
					}
				}
			}
		}
	}
}