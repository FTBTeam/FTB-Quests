package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MessageDeleteObject extends MessageBase {
	private final long id;

	MessageDeleteObject(FriendlyByteBuf buffer) {
		id = buffer.readLong();
	}

	public MessageDeleteObject(long i) {
		id = i;
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