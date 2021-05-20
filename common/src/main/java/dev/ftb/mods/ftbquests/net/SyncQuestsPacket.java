package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class SyncQuestsPacket extends BaseS2CPacket {
	private final UUID self;
	private final QuestFile file;

	SyncQuestsPacket(FriendlyByteBuf buffer) {
		self = buffer.readUUID();
		file = FTBQuests.PROXY.createClientQuestFile();
		file.readNetDataFull(buffer, self);
	}

	public SyncQuestsPacket(UUID s, QuestFile f) {
		self = s;
		file = f;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.SYNC_QUESTS;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(self);
		file.writeNetDataFull(buffer, self);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		file.load(self);
	}
}