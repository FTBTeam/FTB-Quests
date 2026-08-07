package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.CreateOrDeleteRecord;
import dev.ftb.mods.ftbquests.quest.history.events.CreateQuestObjects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Received on: SERVER<br>
 * Sent by client to create one or more brand-new quest objects of any kind. See also {@link CreateQuestAndTaskMessage},
 * which is used for creating a quest and task together.
 */
public record CreateObjectMessage(List<CreateOrDeleteRecord> creationRecords, boolean openScreen) implements CustomPacketPayload {
	public static final Type<CreateObjectMessage> TYPE = new Type<>(FTBQuestsAPI.id("create_object_message"));

	public static final StreamCodec<RegistryFriendlyByteBuf, CreateObjectMessage> STREAM_CODEC = StreamCodec.composite(
			CreateOrDeleteRecord.STREAM_CODEC.apply(ByteBufCodecs.list()), CreateObjectMessage::creationRecords,
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
		List<CreateOrDeleteRecord> records = questObjects.stream().map(CreateOrDeleteRecord::ofQuestObject).toList();
		return new CreateObjectMessage(records, openScreen);
	}

	@Override
	public Type<CreateObjectMessage> type() {
		return TYPE;
	}

	public static void handle(CreateObjectMessage message, PacketContext context) {
		if (PermissionsHelper.canPlayerEdit(context) && context.player() instanceof ServerPlayer sp) {
			ServerQuestFile.ifExists(sqf -> {
				List<CreateOrDeleteRecord> creationRecs = message.creationRecords.stream()
						.filter(rec -> rec.questObjectType() != QuestObjectType.NULL)
						.map(r -> r.withNewID(sqf))
						.toList();
				UUID creatorId = message.openScreen ? sp.getUUID() : null;
				sqf.getHistoryStack().addAndApply(sqf, new CreateQuestObjects(creationRecs, creatorId));
			});
		}
	}
}
