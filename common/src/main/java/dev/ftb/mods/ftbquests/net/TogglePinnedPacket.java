package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseC2SPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class TogglePinnedPacket extends BaseC2SPacket {
	private final long id;

	TogglePinnedPacket(FriendlyByteBuf buffer) {
		id = buffer.readLong();
	}

	public TogglePinnedPacket(long i) {
		id = i;
	}

	@Override
	public PacketID getId() {
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
		new TogglePinnedResponsePacket(id, p).sendTo(player);
	}
}