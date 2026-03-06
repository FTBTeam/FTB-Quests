package dev.ftb.mods.ftbquests.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;

public record TogglePinnedMessage(long id) implements CustomPacketPayload {
	public static final Type<TogglePinnedMessage> TYPE = new Type<>(FTBQuestsAPI.id("toggle_pinned_message"));

	public static final StreamCodec<FriendlyByteBuf, TogglePinnedMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, TogglePinnedMessage::id,
			TogglePinnedMessage::new
	);

	@Override
	public Type<TogglePinnedMessage> type() {
		return TYPE;
	}

	public static void handle(TogglePinnedMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			ServerPlayer player = (ServerPlayer) context.getPlayer();
			ServerQuestFile.getInstance().getTeamData(player).ifPresent(data -> {
				boolean newPinned = !data.isQuestPinned(player, message.id);
				data.setQuestPinned(player, message.id, newPinned);
				NetworkManager.sendToPlayer(player, new TogglePinnedResponseMessage(message.id, newPinned));
			});
		});
	}
}
