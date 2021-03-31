package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.util.QuestKey;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MessageClaimRewardResponse extends MessageBase {
	private final QuestKey key;

	MessageClaimRewardResponse(FriendlyByteBuf buffer) {
		key = QuestKey.of(buffer);
	}

	public MessageClaimRewardResponse(QuestKey k) {
		key = k;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		key.write(buffer);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.claimReward(key);
	}
}