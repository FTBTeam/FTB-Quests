package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.TeamData;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageCreateTeamData extends MessageBase {
	private final UUID uuid;
	private final String name;

	MessageCreateTeamData(FriendlyByteBuf buffer) {
		uuid = buffer.readUUID();
		name = buffer.readUtf(Short.MAX_VALUE);
	}

	public MessageCreateTeamData(TeamData data) {
		uuid = data.uuid;
		name = data.name;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(uuid);
		buffer.writeUtf(name, Short.MAX_VALUE);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.createTeamData(uuid, name);
	}
}