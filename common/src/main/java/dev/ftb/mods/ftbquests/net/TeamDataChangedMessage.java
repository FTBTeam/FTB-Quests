package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.simple.BaseS2CMessage;
import me.shedaniel.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class TeamDataChangedMessage extends BaseS2CMessage {
	private final TeamDataUpdate oldDataUpdate;
	private final TeamDataUpdate newDataUpdate;

	TeamDataChangedMessage(FriendlyByteBuf buffer) {
		oldDataUpdate = new TeamDataUpdate(buffer);
		newDataUpdate = new TeamDataUpdate(buffer);
	}

	public TeamDataChangedMessage(TeamDataUpdate oldData, TeamDataUpdate newData) {
		oldDataUpdate = oldData;
		newDataUpdate = newData;
	}

	@Override
	public MessageType getType() {
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