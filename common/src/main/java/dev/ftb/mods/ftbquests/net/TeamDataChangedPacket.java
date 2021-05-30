package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class TeamDataChangedPacket extends BaseS2CPacket {
	private final TeamDataUpdate oldDataUpdate;
	private final TeamDataUpdate newDataUpdate;

	TeamDataChangedPacket(FriendlyByteBuf buffer) {
		oldDataUpdate = new TeamDataUpdate(buffer);
		newDataUpdate = new TeamDataUpdate(buffer);
	}

	public TeamDataChangedPacket(TeamDataUpdate oldData, TeamDataUpdate newData) {
		oldDataUpdate = oldData;
		newDataUpdate = newData;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.TEAM_DATA_CHANGED;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		oldDataUpdate.write(buffer);
		newDataUpdate.write(buffer);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.teamDataChanged(oldDataUpdate, newDataUpdate);
	}
}