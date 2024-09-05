package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.history.CreateDeleteRecord;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record CreateObjectResponseMessage(List<CreateDeleteRecord> creationRecords, Optional<UUID> creator) implements CustomPacketPayload {
	public static final Type<CreateObjectResponseMessage> TYPE = new Type<>(FTBQuestsAPI.rl("create_object_response_message"));

	public static final StreamCodec<FriendlyByteBuf, CreateObjectResponseMessage> STREAM_CODEC = StreamCodec.composite(
			CreateDeleteRecord.STREAM_CODEC.apply(ByteBufCodecs.list()), CreateObjectResponseMessage::creationRecords,
			ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), CreateObjectResponseMessage::creator,
			CreateObjectResponseMessage::new
	);

	public static CreateObjectResponseMessage create(QuestObjectBase questObject) {
		return create(questObject, null);
	}

	public static CreateObjectResponseMessage create(QuestObjectBase questObject, @Nullable UUID creator) {
		return create(List.of(questObject), creator);
	}

	public static CreateObjectResponseMessage create(Collection<? extends QuestObjectBase> questObjects, @Nullable UUID creator) {
		List<CreateDeleteRecord> creationRecords = questObjects.stream().map(CreateDeleteRecord::ofQuestObject).toList();
		return new CreateObjectResponseMessage(creationRecords, Optional.ofNullable(creator));
	}

	@Override
	public Type<CreateObjectResponseMessage> type() {
		return TYPE;
	}

	public static void handle(CreateObjectResponseMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.createObjects(message.creationRecords, message.creator.orElse(Util.NIL_UUID)));
	}
}