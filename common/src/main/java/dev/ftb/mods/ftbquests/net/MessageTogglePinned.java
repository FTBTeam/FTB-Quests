package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.quest.PlayerData;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class MessageTogglePinned extends MessageBase {
	private final long id;

	MessageTogglePinned(FriendlyByteBuf buffer) {
		id = buffer.readLong();
	}

	public MessageTogglePinned(long i) {
		id = i;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		ServerPlayer player = (ServerPlayer) context.getPlayer();
		PlayerData data = ServerQuestFile.INSTANCE.getData(player);
		data.setQuestPinned(id, !data.isQuestPinned(id));
		new MessageTogglePinnedResponse(id).sendTo(player);
	}
}