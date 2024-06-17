package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DeleteObjectMessage(long id) implements CustomPacketPayload {
	public static final Type<DeleteObjectMessage> TYPE = new Type<>(FTBQuestsAPI.rl("delete_object_message"));

	public static final StreamCodec<FriendlyByteBuf, DeleteObjectMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, DeleteObjectMessage::id,
			DeleteObjectMessage::new
	);

	@Override
	public Type<DeleteObjectMessage> type() {
		return TYPE;
	}

	public static void handle(DeleteObjectMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			if (NetUtils.canEdit(context)) {
				ServerQuestFile.INSTANCE.deleteObject(message.id);
			}
		});
	}
}