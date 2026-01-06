package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.util.NetUtils;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;
import java.util.function.Consumer;

public record ChangeProgressMessage(UUID teamId, ProgressChange progressChange) implements CustomPacketPayload {
	public static final Type<ChangeProgressMessage> TYPE = new Type<>(FTBQuestsAPI.id("change_progress_message"));

	public static final StreamCodec<FriendlyByteBuf, ChangeProgressMessage> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, ChangeProgressMessage::teamId,
			ProgressChange.STREAM_CODEC, ChangeProgressMessage::progressChange,
			ChangeProgressMessage::new
	);

	@Override
	public Type<ChangeProgressMessage> type() {
		return TYPE;
	}

	public static void sendToServer(TeamData team, QuestObjectBase object, Consumer<ProgressChange> changeConsumer) {
		if (!team.isLocked()) {
			ProgressChange change = new ProgressChange(object, FTBQuestsClient.getClientPlayer().getUUID());
			changeConsumer.accept(change);
			NetworkManager.sendToServer(new ChangeProgressMessage(team.getTeamId(), change));
		}
	}

	public static void handle(ChangeProgressMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			if (NetUtils.canEdit(context)) {
				message.progressChange.maybeForceProgress(message.teamId);
			}
		});
	}
}
