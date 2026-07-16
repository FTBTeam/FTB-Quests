package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.events.MoveMovableObject;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MoveMovableMessage(long objectId, long newChapterID, double x, double y) implements CustomPacketPayload {
	public static final Type<MoveMovableMessage> TYPE = new Type<>(FTBQuestsAPI.id("move_movable_message"));

	public static final StreamCodec<FriendlyByteBuf, MoveMovableMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, MoveMovableMessage::objectId,
			ByteBufCodecs.VAR_LONG, MoveMovableMessage::newChapterID,
			ByteBufCodecs.DOUBLE, MoveMovableMessage::x,
			ByteBufCodecs.DOUBLE, MoveMovableMessage::y,
			MoveMovableMessage::new
	);

	@Override
	public Type<MoveMovableMessage> type() {
		return TYPE;
	}

	public static void handle(MoveMovableMessage message, PacketContext context) {
		ServerQuestFile sqf = ServerQuestFile.getInstance();
		if (NetUtils.canEdit(context) && sqf.getBase(message.objectId) instanceof Movable movable) {
			Chapter toChapter = sqf.getChapter(message.newChapterID);
			if (toChapter != null) {
				MoveMovableObject.create(movable, toChapter, message.x(), message.y())
						.ifPresent(event -> sqf.getHistoryStack().addAndApply(sqf, event));
			}
		}
	}
}
