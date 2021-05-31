package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.TeamData;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class SyncTeamDataPacket extends BaseS2CPacket {
	private final boolean self;
	private final TeamData teamData;

	SyncTeamDataPacket(FriendlyByteBuf buffer) {
		self = buffer.readBoolean();
		teamData = new TeamData(buffer.readUUID());
		teamData.read(buffer, self);
	}

	public SyncTeamDataPacket(TeamData d, boolean s) {
		self = s;
		teamData = d;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.SYNC_TEAM_DATA;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(self);
		buffer.writeUUID(teamData.uuid);
		teamData.write(buffer, self);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		teamData.file = FTBQuests.PROXY.getClientQuestFile();
		teamData.file.addData(teamData, true);

		if (self) {
			teamData.file.setSelf(teamData);
		}
	}
}