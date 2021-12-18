package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class TogglePinnedMessage extends BaseC2SMessage {
	private final long id;

	TogglePinnedMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
	}

	public TogglePinnedMessage(long i) {
		id = i;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.TOGGLE_PINNED;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		ServerPlayer player = (ServerPlayer) context.getPlayer();
		TeamData data = ServerQuestFile.INSTANCE.getData(player);
		boolean p = !data.isQuestPinned(id);
		data.setQuestPinned(id, p);
		new TogglePinnedResponseMessage(id, p).sendTo(player);
	}
}