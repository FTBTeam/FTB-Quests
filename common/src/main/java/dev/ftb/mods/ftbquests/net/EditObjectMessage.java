package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.EditRecord;
import dev.ftb.mods.ftbquests.quest.history.HistoryEvent;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;
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
	public static final Type<EditObjectMessage> TYPE = new Type<>(FTBQuestsAPI.rl("edit_object_message"));

	public static final StreamCodec<FriendlyByteBuf, EditObjectMessage> STREAM_CODEC = StreamCodec.composite(
			EditRecord.STREAM_CODEC.apply(ByteBufCodecs.list()), EditObjectMessage::editRecords,
			EditObjectMessage::new
	);

	public static EditObjectMessage forQuestObject(QuestObjectBase qo) {
		return EditObjectMessage.forQuestObjects(List.of(qo));
	}

	public static EditObjectMessage forQuestObjects(Collection<? extends QuestObjectBase> list) {
		return new EditObjectMessage(list.stream().map(EditRecord::ofQuestObject).toList());
	}

	public static void sendToServer(QuestObjectBase qo) {
		NetworkManager.sendToServer(forQuestObject(qo));
	}

	@Override
	public Type<EditObjectMessage> type() {
		return TYPE;
	}

	public static void handle(EditObjectMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			if (NetUtils.canEdit(context)) {
				ServerQuestFile sqf = ServerQuestFile.INSTANCE;
				HistoryEvent.Modification.fromEditRecords(sqf, message.editRecords).ifPresent(modification -> {
					sqf.getHistoryStack().addAndApply(sqf, modification);
					NetworkHelper.sendToAll(context.getPlayer().getServer(), new EditObjectResponseMessage(modification.newRecords()));
				});
//				{
//					QuestObjectBase object = ServerQuestFile.INSTANCE.getBase(editRecord.id());
//					if (object != null) {
//						object.readData(editRecord.nbt(), context.registryAccess());
//						object.editedFromGUIOnServer();
//						object.getQuestFile().clearCachedData();
//						responses.add(object);
//					}
//				});
//				NetworkHelper.sendToAll(context.getPlayer().getServer(), new EditObjectResponseMessage(responses));
//				ServerQuestFile.INSTANCE.markDirty();
			}
		});
	}
}