package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.events.MoveChapterGroup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MoveChapterGroupMessage(long id, boolean movingUp) implements CustomPacketPayload {
	public static final Type<MoveChapterGroupMessage> TYPE = new Type<>(FTBQuestsAPI.id("move_chapter_group_message"));

	public static final StreamCodec<FriendlyByteBuf, MoveChapterGroupMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, MoveChapterGroupMessage::id,
			ByteBufCodecs.BOOL, MoveChapterGroupMessage::movingUp,
			MoveChapterGroupMessage::new
	);

	@Override
	public Type<MoveChapterGroupMessage> type() {
		return TYPE;
	}

	public static void handle(MoveChapterGroupMessage message, PacketContext context) {
		if (PermissionsHelper.canPlayerEdit(context)) {
			ServerQuestFile.ifExists(sqf ->
					sqf.getHistoryStack().addAndApply(sqf, new MoveChapterGroup(message.id, message.movingUp))
			);
		}
	}
}
