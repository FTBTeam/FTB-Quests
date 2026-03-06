package dev.ftb.mods.ftbquests.net;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;

import java.util.UUID;

public record UpdateTeamDataMessage(UUID teamId, String name) implements CustomPacketPayload {
	public static final Type<UpdateTeamDataMessage> TYPE = new Type<>(FTBQuestsAPI.id("update_team_data_message"));

	public static final StreamCodec<FriendlyByteBuf, UpdateTeamDataMessage> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, UpdateTeamDataMessage::teamId,
			ByteBufCodecs.STRING_UTF8, UpdateTeamDataMessage::name,
			UpdateTeamDataMessage::new
	);

	@Override
	public Type<UpdateTeamDataMessage> type() {
		return TYPE;
	}

	public static void handle(UpdateTeamDataMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.updateTeamData(message.teamId, message.name));
	}
}
