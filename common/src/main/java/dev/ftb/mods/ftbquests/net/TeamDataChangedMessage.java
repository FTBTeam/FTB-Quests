package dev.ftb.mods.ftbquests.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;

public record TeamDataChangedMessage(TeamDataUpdate oldDataUpdate, TeamDataUpdate newDataUpdate) implements CustomPacketPayload {
	public static final Type<TeamDataChangedMessage> TYPE = new Type<>(FTBQuestsAPI.id("team_data_changed_message"));

	public static final StreamCodec<FriendlyByteBuf, TeamDataChangedMessage> STREAM_CODEC = StreamCodec.composite(
			TeamDataUpdate.STREAM_CODEC, TeamDataChangedMessage::oldDataUpdate,
			TeamDataUpdate.STREAM_CODEC, TeamDataChangedMessage::newDataUpdate,
			TeamDataChangedMessage::new
	);

	@Override
	public Type<TeamDataChangedMessage> type() {
		return TYPE;
	}

	public static void handle(TeamDataChangedMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.teamDataChanged(message.newDataUpdate));
	}
}
