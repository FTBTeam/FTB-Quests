package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.simple.BaseS2CMessage;
import me.shedaniel.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class SyncQuestsMessage extends BaseS2CMessage {
	private final QuestFile file;

	SyncQuestsMessage(FriendlyByteBuf buffer) {
		file = FTBQuests.PROXY.createClientQuestFile();
		file.readNetDataFull(buffer);
	}

	public SyncQuestsMessage(QuestFile f) {
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
		file.load();
	}
}