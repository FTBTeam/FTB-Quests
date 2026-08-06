package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.CreateOrDeleteRecord;
import dev.ftb.mods.ftbquests.quest.history.events.DeleteQuestObjects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
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
		context.queue(() -> ServerQuestFile.getInstance().ifPresent(sqf -> {
			if (PermissionsHelper.canPlayerEdit(context)) {
				List<CreateOrDeleteRecord> records = expandChildren(sqf, CreateOrDeleteRecord.fromIds(sqf, message.ids));
				if (!records.isEmpty()) {
					sqf.getHistoryStack().addAndApply(sqf, new DeleteQuestObjects(records));
				}
			}
		}));
	}

	/**
	 * Given a list of deletion records, expand the list to include all child objects too, e.g. if there's a quest
	 * in the list, also add its tasks/rewards, *before* the quest itself. Operates recursively where needed.
	 *
	 * @param in the input list
	 * @return the (possibly) expanded, list
	 */
	private static List<CreateOrDeleteRecord> expandChildren(ServerQuestFile file, List<CreateOrDeleteRecord> in) {
		List<CreateOrDeleteRecord> res = new ArrayList<>();

		for (var rec : in) {
			QuestObjectBase qob = file.getBase(rec.id());
			if (qob instanceof QuestObject qo && !(qob instanceof BaseQuestFile)) {
				List<CreateOrDeleteRecord> l = qo.getChildren().stream()
						.flatMap(child -> expandChildren(file, List.of(CreateOrDeleteRecord.ofQuestObject(child))).stream())
						.toList();
                res.addAll(l);
			}
			res.add(rec);
		}

		return res;
	}
}