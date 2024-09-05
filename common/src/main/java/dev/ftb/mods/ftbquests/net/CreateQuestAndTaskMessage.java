package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

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
 * @param nbt the task data NBT
 * @param extra extra initial task data
 */
public record CreateQuestAndTaskMessage(long chapterId, double x, double y, int taskTypeId, CompoundTag nbt, Optional<CompoundTag> extra) implements CustomPacketPayload {
	public static final Type<CreateQuestAndTaskMessage> TYPE = new Type<>(FTBQuestsAPI.rl("create_task_at_message"));

	public static final StreamCodec<FriendlyByteBuf, CreateQuestAndTaskMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, CreateQuestAndTaskMessage::chapterId,
			ByteBufCodecs.DOUBLE, CreateQuestAndTaskMessage::x,
			ByteBufCodecs.DOUBLE, CreateQuestAndTaskMessage::y,
			ByteBufCodecs.VAR_INT, CreateQuestAndTaskMessage::taskTypeId,
			ByteBufCodecs.COMPOUND_TAG, CreateQuestAndTaskMessage::nbt,
			ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG), CreateQuestAndTaskMessage::extra,
			CreateQuestAndTaskMessage::new
	);

	public static CreateQuestAndTaskMessage requestCreation(Chapter chapter, double x, double y, Task task) {
		return new CreateQuestAndTaskMessage(chapter.id, x, y, task.getType().internalId,
				Util.make(new CompoundTag(), nbt1 -> task.writeData(nbt1, chapter.getQuestFile().holderLookup())),
                Optional.ofNullable(task.makeExtraCreationData())
		);
	}

	@Override
	public Type<CreateQuestAndTaskMessage> type() {
		return TYPE;
	}

	public static void handle(CreateQuestAndTaskMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			if (NetUtils.canEdit(context) && context.getPlayer() instanceof ServerPlayer sp) {
				ServerQuestFile file = ServerQuestFile.INSTANCE;
				Chapter chapter = file.getChapter(message.chapterId);
				TaskType taskType = ServerQuestFile.INSTANCE.getTaskType(message.taskTypeId);

				if (chapter != null && taskType != null) {
					Quest quest = new Quest(file.newID(), chapter);
					quest.setX(message.x);
					quest.setY(message.y);
					quest.onCreated();

					Task task = taskType.createTask(file.newID(), quest);
					task.readData(message.nbt, context.registryAccess());
					task.onCreated();
					CompoundTag extra = message.extra.orElse(new CompoundTag());
					file.getTranslationManager().processInitialTranslation(extra, task);

					NetworkHelper.sendToAll(sp.getServer(), CreateObjectResponseMessage.create(List.of(quest, task), sp.getUUID()));

					file.markDirty();
				}
			}
		});
	}
}