package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.simple.BaseS2CMessage;
import me.shedaniel.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class CreateOtherTeamDataMessage extends BaseS2CMessage {
	private final TeamDataUpdate dataUpdate;

	CreateOtherTeamDataMessage(FriendlyByteBuf buffer) {
		dataUpdate = new TeamDataUpdate(buffer);
	}

	public CreateOtherTeamDataMessage(TeamDataUpdate update) {
		dataUpdate = update;
	}

	@Override
	public MessageType getType() {
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