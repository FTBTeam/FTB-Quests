package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MessageDeleteObjectResponse extends MessageBase {
	private final long id;

	MessageDeleteObjectResponse(FriendlyByteBuf buffer) {
		id = buffer.readLong();
	}

	public MessageDeleteObjectResponse(long i) {
		id = i;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.deleteObject(id);
	}
}