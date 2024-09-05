package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
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
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Received on: SERVER<br>
 * Sent by client to create one or more brand-new quest objects of any kind. See also {@link CreateQuestAndTaskMessage},
 * which is used for creating a quest and task together.
 */
public record CreateObjectMessage(List<CreateDeleteRecord> creationRecords, boolean openScreen) implements CustomPacketPayload {
	public static final Type<CreateObjectMessage> TYPE = new Type<>(FTBQuestsAPI.rl("create_object_message"));

	public static final StreamCodec<FriendlyByteBuf, CreateObjectMessage> STREAM_CODEC = StreamCodec.composite(
			CreateDeleteRecord.STREAM_CODEC.apply(ByteBufCodecs.list()), CreateObjectMessage::creationRecords,
			ByteBufCodecs.BOOL, CreateObjectMessage::openScreen,
			CreateObjectMessage::new
	);

	public static CreateObjectMessage requestCreation(QuestObjectBase questObject) {
		return requestCreation(questObject, true);
	}

	public static CreateObjectMessage requestCreation(QuestObjectBase questObject, boolean openScreen) {
		return requestCreation(List.of(questObject), openScreen);
	}

	public static CreateObjectMessage requestCreation(Collection<? extends QuestObjectBase> questObjects, boolean openScreen) {
		List<CreateDeleteRecord> records = questObjects.stream().map(CreateDeleteRecord::ofQuestObject).toList();
		return new CreateObjectMessage(records, openScreen);
	}

	@Override
	public Type<CreateObjectMessage> type() {
		return TYPE;
	}

	public static void handle(CreateObjectMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			if (NetUtils.canEdit(context) && context.getPlayer() instanceof ServerPlayer sp) {
				message.creationRecords.forEach(creationRecord -> {
					ServerQuestFile sqf = ServerQuestFile.INSTANCE;

					// records will have arrived from client with an id of 0, so allocate some real id's now
					List<CreateDeleteRecord> creationRecs = message.creationRecords.stream().map(r -> r.withNewID(sqf)).toList();

					sqf.getHistoryStack().addAndApply(sqf, new HistoryEvent.Creation(creationRecs));

					NetworkHelper.sendToAll(sp.getServer(), new CreateObjectResponseMessage(creationRecs, Optional.ofNullable(message.openScreen ? sp.getUUID() : null)));
				});
			}
		});
	}
}