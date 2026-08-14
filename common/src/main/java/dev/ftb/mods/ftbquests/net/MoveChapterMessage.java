package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.events.MoveChapterWithinGroup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MoveChapterMessage(long id, boolean movingUp) implements CustomPacketPayload {
	public static final Type<MoveChapterMessage> TYPE = new Type<>(FTBQuestsAPI.rl("move_chapter_message"));

	public static final StreamCodec<FriendlyByteBuf, MoveChapterMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, MoveChapterMessage::id,
			ByteBufCodecs.BOOL, MoveChapterMessage::movingUp,
			MoveChapterMessage::new
	);

	@Override
	public Type<MoveChapterMessage> type() {
		return TYPE;
	}

	public static void handle(MoveChapterMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> ServerQuestFile.getInstance().ifPresent(sqf -> {
			if (PermissionsHelper.canPlayerEdit(context)) {
				sqf.getHistoryStack().addAndApply(sqf, new MoveChapterWithinGroup(message.id, message.movingUp));
			}
		}));
	}
}