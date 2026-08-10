package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.EditRecord;
import dev.ftb.mods.ftbquests.quest.history.QuestBookEditEvent;
import dev.ftb.mods.ftbquests.quest.history.events.ModifyQuestObjects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Collection;
import java.util.List;

/**
 * Received on: SERVER
 * <br>
 * Sent by client whenever any quest object is being edited client-side.
 *
 * @param editRecords one or more of (questobject id, serialized questobject data)
 */
public record EditObjectMessage(List<EditRecord> editRecords) implements CustomPacketPayload {
	public static final Type<EditObjectMessage> TYPE = new Type<>(FTBQuestsAPI.id("edit_object_message"));

	public static final StreamCodec<RegistryFriendlyByteBuf, EditObjectMessage> STREAM_CODEC = StreamCodec.composite(
			EditRecord.STREAM_CODEC.apply(ByteBufCodecs.list()), EditObjectMessage::editRecords,
			EditObjectMessage::new
	);

	public static EditObjectMessage forQuestObject(QuestObjectBase qo) {
		return EditObjectMessage.forQuestObjects(List.of(qo));
	}

	public static EditObjectMessage forQuestObjects(Collection<? extends QuestObjectBase> list) {
		return new EditObjectMessage(list.stream().limit(QuestBookEditEvent.MAX_LIST_SIZE).map(EditRecord::ofQuestObject).toList());
	}

	public static void sendToServer(QuestObjectBase qo) {
		Play2ServerNetworking.send(forQuestObject(qo));
	}

	@Override
	public Type<EditObjectMessage> type() {
		return TYPE;
	}

	public static void handle(EditObjectMessage message, PacketContext context) {
		if (PermissionsHelper.canPlayerEdit(context)) {
			ServerQuestFile.ifExists(sqf ->
					ModifyQuestObjects.fromEditRecords(sqf, QuestBookEditEvent.takeLimitedElements(message.editRecords))
							.ifPresent(modification -> sqf.getHistoryStack().addAndApply(sqf, modification)));
		}
	}
}
