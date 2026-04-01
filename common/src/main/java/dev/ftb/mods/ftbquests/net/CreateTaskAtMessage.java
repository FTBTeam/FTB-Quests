package dev.ftb.mods.ftbquests.net;

import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5NetPacker;
import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public record CreateTaskAtMessage(long chapterId, double x, double y, int taskTypeId, Json5Element payloadJson, Optional<Json5Element> extraJson) implements CustomPacketPayload {
	public static final Type<CreateTaskAtMessage> TYPE = new Type<>(FTBQuestsAPI.id("create_task_at_message"));

	public static final StreamCodec<FriendlyByteBuf, CreateTaskAtMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, CreateTaskAtMessage::chapterId,
			ByteBufCodecs.DOUBLE, CreateTaskAtMessage::x,
			ByteBufCodecs.DOUBLE, CreateTaskAtMessage::y,
			ByteBufCodecs.VAR_INT, CreateTaskAtMessage::taskTypeId,
			Json5NetPacker.CODEC, CreateTaskAtMessage::payloadJson,
			ByteBufCodecs.optional(Json5NetPacker.CODEC), CreateTaskAtMessage::extraJson,
			CreateTaskAtMessage::new
	);

	public static CreateTaskAtMessage create(Chapter chapter, double x, double y, Task task, @Nullable Json5Element extra) {
		return new CreateTaskAtMessage(chapter.id, x, y, task.getType().internalId,
				Util.make(new Json5Object(), o -> task.writeData(o, chapter.getQuestFile().holderLookup())),
				Optional.ofNullable(extra)
		);
	}

	@Override
	public Type<CreateTaskAtMessage> type() {
		return TYPE;
	}

	public static void handle(CreateTaskAtMessage message, PacketContext context) {
		if (NetUtils.canEdit(context) && context.player() instanceof ServerPlayer sp && message.payloadJson instanceof Json5Object json) {
			ServerQuestFile file = ServerQuestFile.getInstance();
			Chapter chapter = file.getChapter(message.chapterId);
			TaskType taskType = ServerQuestFile.getInstance().getTaskType(message.taskTypeId);

			if (chapter != null) {
				Quest quest = new Quest(file.newID(), chapter);
				quest.setX(message.x);
				quest.setY(message.y);
				quest.onCreated();
				Server2PlayNetworking.sendToAllPlayers(sp.level().getServer(), CreateObjectResponseMessage.create(quest, null));

				Task task = taskType.createTask(file.newID(), quest);
				task.readData(json, context.player().registryAccess());
				task.onCreated();
				Json5Object extra = NetUtils.jsonObjectFromOptionalElement(message.extraJson);
				file.getTranslationManager().processInitialTranslation(extra, task);
				extra.addProperty("type", taskType.getTypeForSerialization());
				Server2PlayNetworking.sendToAllPlayers(sp.level().getServer(), CreateObjectResponseMessage.create(task, extra, sp.getUUID()));

				file.refreshIDMap();
				file.clearCachedData();
				file.markDirty();
			}
		}
	}
}
