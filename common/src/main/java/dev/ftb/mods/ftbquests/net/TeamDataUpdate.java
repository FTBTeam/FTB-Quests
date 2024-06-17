package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

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
