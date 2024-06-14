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

import java.util.Optional;

public record CreateTaskAtMessage(long chapterId, double x, double y, int taskTypeId, CompoundTag nbt, Optional<CompoundTag> extra) implements CustomPacketPayload {
	public static final Type<CreateTaskAtMessage> TYPE = new Type<>(FTBQuestsAPI.rl("create_task_at_message"));

	public static final StreamCodec<FriendlyByteBuf, CreateTaskAtMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, CreateTaskAtMessage::chapterId,
			ByteBufCodecs.DOUBLE, CreateTaskAtMessage::x,
			ByteBufCodecs.DOUBLE, CreateTaskAtMessage::y,
			ByteBufCodecs.VAR_INT, CreateTaskAtMessage::taskTypeId,
			ByteBufCodecs.COMPOUND_TAG, CreateTaskAtMessage::nbt,
			ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG), CreateTaskAtMessage::extra,
			CreateTaskAtMessage::new
	);

	public static CreateTaskAtMessage create(Chapter chapter, double x, double y, Task task, CompoundTag extra) {
		return new CreateTaskAtMessage(chapter.id, x, y, task.getType().internalId,
				Util.make(new CompoundTag(), nbt1 -> task.writeData(nbt1, chapter.getQuestFile().holderLookup())),
                Optional.ofNullable(extra)
		);
	}

	@Override
	public Type<CreateTaskAtMessage> type() {
		return TYPE;
	}

	public static void handle(CreateTaskAtMessage message, NetworkManager.PacketContext context) {
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
					NetworkHelper.sendToAll(sp.getServer(), CreateObjectResponseMessage.create(quest, null));

					Task task = taskType.createTask(file.newID(), quest);
					task.readData(message.nbt, context.registryAccess());
					task.onCreated();
					CompoundTag extra = message.extra.orElse(new CompoundTag());
					file.getTranslationManager().processInitialTranslation(extra, task);
					extra.putString("type", taskType.getTypeForNBT());
					NetworkHelper.sendToAll(sp.getServer(), CreateObjectResponseMessage.create(task, extra, sp.getUUID()));

					file.refreshIDMap();
					file.clearCachedData();
					file.markDirty();
				}
			}
		});
	}
}