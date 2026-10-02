package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.history.CreateOrDeleteRecord;
import dev.ftb.mods.ftbquests.quest.history.EditRecord;
import dev.ftb.mods.ftbquests.quest.history.QuestBookEditEvent;
import dev.ftb.mods.ftbquests.quest.history.events.CompositeEditEvent;
import dev.ftb.mods.ftbquests.quest.history.events.DeleteQuestObjects;
import dev.ftb.mods.ftbquests.quest.history.events.ModifyQuestObjects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

public record DeleteObjectMessage(List<Long> ids) implements CustomPacketPayload {
	public static final Type<DeleteObjectMessage> TYPE = new Type<>(FTBQuestsAPI.id("delete_object_message"));

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

	public static void handle(DeleteObjectMessage message, PacketContext context) {
		ServerQuestFile.ifExists(sqf -> {
			if (PermissionsHelper.canPlayerEdit(context)) {
				List<Long> idSublist = QuestBookEditEvent.takeLimitedElements(message.ids);
				// LinkedHashSet deduplicates input ids while preserving order, which may be significant
				List<CreateOrDeleteRecord> records = expandChildren(sqf, CreateOrDeleteRecord.fromIds(sqf, new LinkedHashSet<>(idSublist)));
				if (!records.isEmpty()) {
					DeleteQuestObjects deleteEvent = new DeleteQuestObjects(records);
					cleanUpQuestDependencies(sqf, records).ifPresentOrElse(
							modifyEvent -> sqf.getHistoryStack().addAndApply(sqf,
									CompositeEditEvent.of(modifyEvent, deleteEvent)
							),
							() -> sqf.getHistoryStack().addAndApply(sqf, deleteEvent));

				}
			}
		});
	}

    private static Optional<ModifyQuestObjects> cleanUpQuestDependencies(ServerQuestFile file, List<CreateOrDeleteRecord> in) {
		// If we're removing a quest, then we also need to record the modification of any dependent quests of this quest,
		//   removing the to-be-deleted quest from their dependency list
		// This ensures that a later undo can restore that dependency
		List<EditRecord> oldRecs = new ArrayList<>();
		List<EditRecord> newRecs = new ArrayList<>();
        for (var rec : in) {
			if (file.getBase(rec.id()) instanceof Quest questToDelete) {
				questToDelete.getDependants().forEach(dep -> {
					if (dep instanceof Quest depQuest) {
						oldRecs.add(EditRecord.ofQuestObject(depQuest));
						Quest depCopy = QuestObjectBase.copy(depQuest, () -> new Quest(depQuest.getId(), depQuest.getQuestChapter()));
						depCopy.removeDependency(questToDelete);
						newRecs.add(EditRecord.ofQuestObject(depCopy));
					}
				});
			}
		}
		return oldRecs.isEmpty() ? Optional.empty() : Optional.of(new ModifyQuestObjects(oldRecs, newRecs));
    }

	/**
	 * Given a list of deletion records, expand the list to include all child objects too, e.g. if there's a quest
	 * in the list, also add its tasks/rewards, *before* the quest itself. Operates recursively where needed. Does not
	 * allow deletion of the entire quest file.
	 *
	 * @param in the input list
	 * @return the (possibly) expanded, list
	 */
	private static List<CreateOrDeleteRecord> expandChildren(ServerQuestFile file, List<CreateOrDeleteRecord> in) {
		List<CreateOrDeleteRecord> res = new ArrayList<>();

		for (var rec : in) {
			QuestObjectBase qob = file.getBase(rec.id());
			if (qob instanceof QuestObject qo && (!(qob instanceof BaseQuestFile))) {
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