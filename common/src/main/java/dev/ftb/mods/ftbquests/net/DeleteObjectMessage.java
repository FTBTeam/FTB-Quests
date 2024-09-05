package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.CreateDeleteRecord;
import dev.ftb.mods.ftbquests.quest.history.HistoryEvent;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public record DeleteObjectMessage(List<Long> ids) implements CustomPacketPayload {
	public static final Type<DeleteObjectMessage> TYPE = new Type<>(FTBQuestsAPI.rl("delete_object_message"));

	public static final StreamCodec<FriendlyByteBuf, DeleteObjectMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list()), DeleteObjectMessage::ids,
			DeleteObjectMessage::new
	);

	public static DeleteObjectMessage forQuestObject(QuestObjectBase qo) {
		return new DeleteObjectMessage(List.of(qo.id));
	}

	@Override
	public Type<DeleteObjectMessage> type() {
		return TYPE;
	}

	public static void handle(DeleteObjectMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			if (NetUtils.canEdit(context)) {
				ServerQuestFile sqf = ServerQuestFile.INSTANCE;
				List<CreateDeleteRecord> records = CreateDeleteRecord.fromIds(sqf, message.ids);
				if (!records.isEmpty()) {
					sqf.getHistoryStack().addAndApply(sqf, new HistoryEvent.Deletion(records));
				}
//				sqf.deleteObject(message.id);
			}
		});
	}
}