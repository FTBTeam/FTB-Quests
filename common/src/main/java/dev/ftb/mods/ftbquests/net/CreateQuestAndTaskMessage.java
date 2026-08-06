package dev.ftb.mods.ftbquests.net;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.CreateOrDeleteRecord;
import dev.ftb.mods.ftbquests.quest.history.events.CreateQuestObjects;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.util.FTBQCodecs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;

import java.util.List;
import java.util.Optional;

/**
 * Received on: SERVER<br>
 * Sent by client to create a new quest and task from the GUI.
 *
 * @param chapterId chapter in which the new quest belongs
 * @param x X-position of the new quest
 * @param y Y-position of the new quest
 * @param taskTypeId internal ID for the task type
 * @param data the task data NBT
 * @param metadata extra initial task data
 */
public record CreateQuestAndTaskMessage(long chapterId, double x, double y, int taskTypeId, Json5Object data, Optional<Json5Object> metadata) implements CustomPacketPayload {
	public static final Type<CreateQuestAndTaskMessage> TYPE = new Type<>(FTBQuestsAPI.id("create_task_at_message"));

	public static final StreamCodec<FriendlyByteBuf, CreateQuestAndTaskMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, CreateQuestAndTaskMessage::chapterId,
			ByteBufCodecs.DOUBLE, CreateQuestAndTaskMessage::x,
			ByteBufCodecs.DOUBLE, CreateQuestAndTaskMessage::y,
			ByteBufCodecs.VAR_INT, CreateQuestAndTaskMessage::taskTypeId,
			FTBQCodecs.JSON5_OBJECT_STREAM_CODEC, CreateQuestAndTaskMessage::data,
			ByteBufCodecs.optional(FTBQCodecs.JSON5_OBJECT_STREAM_CODEC), CreateQuestAndTaskMessage::metadata,
			CreateQuestAndTaskMessage::new
	);

	public static CreateQuestAndTaskMessage requestCreation(Chapter chapter, double x, double y, Task task) {
		return new CreateQuestAndTaskMessage(chapter.id, x, y, task.getType().internalId,
				Util.make(new Json5Object(), json -> task.writeData(json, chapter.getQuestFile().holderLookup())),
				Optional.of(task.makeCreationMetadata())
		);
	}

	@Override
	public Type<CreateQuestAndTaskMessage> type() {
		return TYPE;
	}

	public static void handle(CreateQuestAndTaskMessage message, PacketContext context) {
		ServerQuestFile sqf = ServerQuestFile.getInstance();
		if (PermissionsHelper.canPlayerEdit(context) && context.player() instanceof ServerPlayer sp) {
			Chapter chapter = sqf.getChapter(message.chapterId);
			TaskType taskType = sqf.getTaskType(message.taskTypeId);

			if (chapter != null && taskType != null) {
				Quest quest = new Quest(sqf.newID(), chapter).setPosition(message.x, message.y);

				Task task = taskType.createTask(sqf.newID(), quest);
				task.readData(message.data, sp.registryAccess());
				Json5Object metadata = message.metadata.orElse(new Json5Object());
				sqf.getTranslationManager().processInitialTranslation(metadata, task);

				List<CreateOrDeleteRecord> recs = CreateOrDeleteRecord.ofQuestObjects(quest, task);
				sqf.getHistoryStack().addAndApply(sqf, new CreateQuestObjects(recs, sp.getUUID()));
			}
		}
	}
}