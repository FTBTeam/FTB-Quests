package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageSyncQuests extends MessageBase {
	private final UUID self;
	private final QuestFile file;

	MessageSyncQuests(FriendlyByteBuf buffer) {
		self = NetUtils.readUUID(buffer);
		file = FTBQuests.PROXY.createClientQuestFile();
		file.readNetDataFull(buffer, self);
	}

	public MessageSyncQuests(UUID s, QuestFile f) {
		self = s;
		file = f;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		NetUtils.writeUUID(buffer, self);
		file.writeNetDataFull(buffer, self);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		file.load(self);
	}
}