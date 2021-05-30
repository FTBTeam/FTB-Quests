package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class CreateOtherTeamDataPacket extends BaseS2CPacket {
	private final TeamDataUpdate dataUpdate;

	CreateOtherTeamDataPacket(FriendlyByteBuf buffer) {
		dataUpdate = new TeamDataUpdate(buffer);
	}

	public CreateOtherTeamDataPacket(TeamDataUpdate update) {
		dataUpdate = update;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.CREATE_OTHER_TEAM_DATA;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		dataUpdate.write(buffer);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.createOtherTeamData(dataUpdate);
	}
}