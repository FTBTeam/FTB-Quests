package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageChangeProgressResponse extends MessageBase {
	private final UUID team;
	private final ProgressChange progressChange;

	MessageChangeProgressResponse(FriendlyByteBuf buffer) {
		team = buffer.readUUID();
		progressChange = new ProgressChange(ClientQuestFile.INSTANCE, buffer);
	}

	public MessageChangeProgressResponse(UUID t, ProgressChange p) {
		team = t;
		progressChange = p;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(team);
		progressChange.write(buffer);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.changeProgress(team, progressChange);
	}
}