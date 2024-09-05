package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.history.EditRecord;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Collection;
import java.util.List;

/**
 * Received on: CLIENT
 * <br>
 * Sent by server in response to a {@link EditObjectMessage} packet.
 *
 * @param editRecords one or more of (questobject id, serialized questobject data)
 */
public record EditObjectResponseMessage(List<EditRecord> editRecords) implements CustomPacketPayload {
	public static final Type<EditObjectResponseMessage> TYPE = new Type<>(FTBQuestsAPI.rl("edit_object_response_message"));

	public static final StreamCodec<FriendlyByteBuf, EditObjectResponseMessage> STREAM_CODEC = StreamCodec.composite(
			EditRecord.STREAM_CODEC.apply(ByteBufCodecs.list()), EditObjectResponseMessage::editRecords,
			EditObjectResponseMessage::new
	);

	public EditObjectResponseMessage(QuestObjectBase qo) {
		this(List.of(qo));
	}

	public EditObjectResponseMessage(Collection<? extends QuestObjectBase> list) {
		this(list.stream().map(EditRecord::ofQuestObject).toList());
	}

	@Override
	public Type<EditObjectResponseMessage> type() {
		return TYPE;
	}

	public static void handle(EditObjectResponseMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
            message.editRecords.forEach(editRecord -> FTBQuestsNetClient.editObject(editRecord.id(), editRecord.nbt()));
        });
	}
}