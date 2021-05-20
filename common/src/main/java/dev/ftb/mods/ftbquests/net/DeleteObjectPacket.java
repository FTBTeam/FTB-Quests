package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseC2SPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class DeleteObjectPacket extends BaseC2SPacket {
	private final long id;

	DeleteObjectPacket(FriendlyByteBuf buffer) {
		id = buffer.readLong();
	}

	public DeleteObjectPacket(long i) {
		id = i;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.DELETE_OBJECT;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		if (NetUtils.canEdit(context)) {
			ServerQuestFile.INSTANCE.deleteObject(id);
		}
	}
}