package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class SyncQuestsMessage extends BaseS2CMessage {
	private final BaseQuestFile file;

	SyncQuestsMessage(FriendlyByteBuf buffer) {
		file = FTBQuestsClient.createClientQuestFile();
		file.readNetDataFull(buffer);
	}

	public SyncQuestsMessage(BaseQuestFile f) {
		file = f;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.SYNC_QUESTS;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		file.writeNetDataFull(buffer);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		ClientQuestFile.syncFromServer(file);

		ClientQuestFile.INSTANCE.updateLootCrates();

		new RequestTeamDataMessage().sendToServer();
	}
}