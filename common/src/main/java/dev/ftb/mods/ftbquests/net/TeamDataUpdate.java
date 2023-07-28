package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class TeamDataUpdate {
	public final UUID uuid;
	public final String name;

	public TeamDataUpdate(FriendlyByteBuf buffer) {
		uuid = buffer.readUUID();
		name = buffer.readUtf(Short.MAX_VALUE);
	}

	public TeamDataUpdate(TeamData data) {
		uuid = data.getTeamId();
		name = data.getName();
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(uuid);
		buffer.writeUtf(name, Short.MAX_VALUE);
	}
}
