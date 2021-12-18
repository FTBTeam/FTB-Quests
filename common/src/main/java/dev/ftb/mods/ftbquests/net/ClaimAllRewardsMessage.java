package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class ClaimAllRewardsMessage extends BaseC2SMessage {
	public ClaimAllRewardsMessage() {
	}

	ClaimAllRewardsMessage(FriendlyByteBuf buffer) {
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.CLAIM_ALL_REWARDS;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		ServerPlayer player = (ServerPlayer) context.getPlayer();
		TeamData data = TeamData.get(player);

		for (ChapterGroup group : ServerQuestFile.INSTANCE.chapterGroups) {
			for (Chapter chapter : group.chapters) {
				for (Quest quest : chapter.quests) {
					if (data.isCompleted(quest)) {
						for (Reward reward : quest.rewards) {
							if (!reward.getExcludeFromClaimAll()) {
								data.claimReward(player, reward, true);
							}
						}
					}
				}
			}
		}
	}
}