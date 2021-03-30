package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.PlayerData;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class MessageClaimAllRewards extends MessageBase {
	public MessageClaimAllRewards() {
	}

	MessageClaimAllRewards(FriendlyByteBuf buffer) {
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		ServerPlayer player = (ServerPlayer) context.getPlayer();
		PlayerData data = PlayerData.get(player);

		for (ChapterGroup group : ServerQuestFile.INSTANCE.chapterGroups) {
			for (Chapter chapter : group.chapters) {
				for (Quest quest : chapter.quests) {
					if (data.isComplete(quest)) {
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