package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MoveMovableMessage(long id, long chapterID, double x, double y) implements CustomPacketPayload {
	public static final Type<MoveMovableMessage> TYPE = new Type<>(FTBQuestsAPI.id("move_movable_message"));

	public static final StreamCodec<FriendlyByteBuf, MoveMovableMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, MoveMovableMessage::id,
			ByteBufCodecs.VAR_LONG, MoveMovableMessage::chapterID,
			ByteBufCodecs.DOUBLE, MoveMovableMessage::x,
			ByteBufCodecs.DOUBLE, MoveMovableMessage::y,
			MoveMovableMessage::new
	);

	@Override
	public Type<MoveMovableMessage> type() {
		return TYPE;
	}

	public static void handle(MoveMovableMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			if (ServerQuestFile.INSTANCE.get(message.id) instanceof Movable movable) {
				movable.onMoved(message.x, message.y, message.chapterID);
				ServerQuestFile.INSTANCE.markDirty();
				NetworkHelper.sendToAll(ServerQuestFile.INSTANCE.server, new MoveMovableResponseMessage(movable.getMovableID(), message.chapterID, message.x, message.y));
			}
		});
	}
}
