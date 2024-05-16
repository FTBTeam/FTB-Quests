package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncTeamDataMessage(TeamData teamData) implements CustomPacketPayload {
    public static final Type<SyncTeamDataMessage> TYPE = new Type<>(FTBQuestsAPI.rl("sync_team_data_message"));

    public static final StreamCodec<FriendlyByteBuf, SyncTeamDataMessage> STREAM_CODEC = StreamCodec.composite(
        TeamData.STREAM_CODEC, SyncTeamDataMessage::teamData,
	    SyncTeamDataMessage::new
    );

	@Override
    public Type<SyncTeamDataMessage> type() {
        return TYPE;
	}

    public static void handle(SyncTeamDataMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.syncTeamData(message.teamData));
	}
}