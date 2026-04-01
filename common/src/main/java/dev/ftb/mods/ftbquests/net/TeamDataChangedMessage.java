package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

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

	public static void handle(TeamDataChangedMessage message, PacketContext ignoredContext) {
		FTBQuestsNetClient.teamDataChanged(message.newDataUpdate);
	}

	public record TeamDataUpdate(UUID uuid, String name) {
		public static StreamCodec<FriendlyByteBuf, TeamDataUpdate> STREAM_CODEC = StreamCodec.composite(
				UUIDUtil.STREAM_CODEC, TeamDataUpdate::uuid,
				ByteBufCodecs.STRING_UTF8, TeamDataUpdate::name,
				TeamDataUpdate::new
		);

		public static TeamDataUpdate forTeamData(TeamData data) {
			return new TeamDataUpdate(data.getTeamId(), data.getName());
		}
	}
}
