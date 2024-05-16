package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public record CreateObjectResponseMessage(long id, long parent, QuestObjectType questObjectType, CompoundTag nbt, Optional<CompoundTag> extra, Optional<UUID> creator) implements CustomPacketPayload {
	public static final Type<CreateObjectResponseMessage> TYPE = new Type<>(FTBQuestsAPI.rl("create_object_response_message"));

	public static final StreamCodec<FriendlyByteBuf, CreateObjectResponseMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, CreateObjectResponseMessage::id,
			ByteBufCodecs.VAR_LONG, CreateObjectResponseMessage::parent,
			QuestObjectType.STREAM_CODEC, CreateObjectResponseMessage::questObjectType,
			ByteBufCodecs.COMPOUND_TAG, CreateObjectResponseMessage::nbt,
			ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG), CreateObjectResponseMessage::extra,
			ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), CreateObjectResponseMessage::creator,
			CreateObjectResponseMessage::new
	);

	public static CreateObjectResponseMessage create(QuestObjectBase questObject, @Nullable CompoundTag extra) {
		return create(questObject, extra, null);
	}

	public static CreateObjectResponseMessage create(QuestObjectBase questObject, @Nullable CompoundTag extra, @Nullable UUID creator) {
		return new CreateObjectResponseMessage(
				questObject.id,
				questObject.getParentID(),
				questObject.getObjectType(),
				Util.make(new CompoundTag(), nbt1 -> questObject.writeData(nbt1, questObject.getQuestFile().holderLookup())),
                Optional.ofNullable(extra),
				Optional.ofNullable(creator)
		);
	}

	@Override
	public Type<CreateObjectResponseMessage> type() {
		return TYPE;
	}

	public static void handle(CreateObjectResponseMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.createObject(
				message.id, message.parent, message.questObjectType, message.nbt,
				message.extra.orElse(new CompoundTag()), message.creator.orElse(Util.NIL_UUID)
		));
	}
}