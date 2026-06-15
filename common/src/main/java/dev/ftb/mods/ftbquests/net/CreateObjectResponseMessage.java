package dev.ftb.mods.ftbquests.net;

import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5NetPacker;
import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public record CreateObjectResponseMessage(long id, long parent, QuestObjectType questObjectType, Json5Element payloadJson, Optional<Json5Element> extraJson, Optional<UUID> creator) implements CustomPacketPayload {
	public static final Type<CreateObjectResponseMessage> TYPE = new Type<>(FTBQuestsAPI.id("create_object_response_message"));

	public static final StreamCodec<FriendlyByteBuf, CreateObjectResponseMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, CreateObjectResponseMessage::id,
			ByteBufCodecs.VAR_LONG, CreateObjectResponseMessage::parent,
			QuestObjectType.STREAM_CODEC, CreateObjectResponseMessage::questObjectType,
			Json5NetPacker.CODEC, CreateObjectResponseMessage::payloadJson,
			ByteBufCodecs.optional(Json5NetPacker.CODEC), CreateObjectResponseMessage::extraJson,
			ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), CreateObjectResponseMessage::creator,
			CreateObjectResponseMessage::new
	);

	public static CreateObjectResponseMessage create(QuestObjectBase questObject, @Nullable Json5Element extra) {
		return create(questObject, extra, null);
	}

	public static CreateObjectResponseMessage create(QuestObjectBase questObject, @Nullable Json5Element extra, @Nullable UUID creator) {
		return new CreateObjectResponseMessage(
				questObject.id,
				questObject.getParentID(),
				questObject.getObjectType(),
				Util.make(new Json5Object(), o -> questObject.writeData(o, questObject.getQuestFile().holderLookup())),
				Optional.ofNullable(extra),
				Optional.ofNullable(creator)
		);
	}

	@Override
	public Type<CreateObjectResponseMessage> type() {
		return TYPE;
	}

	public static void handle(CreateObjectResponseMessage message, PacketContext ignoredContext) {
		if (message.payloadJson instanceof Json5Object json) {
			FTBQuestsNetClient.createObject(
					message.id, message.parent, message.questObjectType,
					json, NetUtils.jsonObjectFromOptionalElement(message.extraJson),
					message.creator.orElse(Util.NIL_UUID)
			);
		}
	}
}
