package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record CreateObjectMessage(long parent, QuestObjectType questObjectType, boolean openScreen, CompoundTag nbt, Optional<CompoundTag> extra) implements CustomPacketPayload {
	public static final Type<CreateObjectMessage> TYPE = new Type<>(FTBQuestsAPI.rl("create_object_message"));

	public static final StreamCodec<FriendlyByteBuf, CreateObjectMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, CreateObjectMessage::parent,
			QuestObjectType.STREAM_CODEC, CreateObjectMessage::questObjectType,
			ByteBufCodecs.BOOL, CreateObjectMessage::openScreen,
			ByteBufCodecs.COMPOUND_TAG, CreateObjectMessage::nbt,
			ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG), CreateObjectMessage::extra,
			CreateObjectMessage::new
	);

	public static CreateObjectMessage create(QuestObjectBase questObject, @Nullable CompoundTag extra, boolean openScreen) {
		return new CreateObjectMessage(questObject.getParentID(),
				questObject.getObjectType(),
				openScreen,
				Util.make(new CompoundTag(), nbt1 -> questObject.writeData(nbt1, questObject.getQuestFile().holderLookup())),
                Optional.ofNullable(extra)
		);
	}

	public static CreateObjectMessage create(QuestObjectBase questObject, @Nullable CompoundTag extra) {
		return create(questObject, extra, true);
	}

	@Override
	public Type<CreateObjectMessage> type() {
		return TYPE;
	}

	public static void handle(CreateObjectMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			if (NetUtils.canEdit(context) && context.getPlayer() instanceof ServerPlayer sp) {
				QuestObjectBase object = ServerQuestFile.INSTANCE.create(
						ServerQuestFile.INSTANCE.newID(), message.questObjectType, message.parent,
						message.extra.orElse(new CompoundTag())
				);
				object.readData(message.nbt, context.registryAccess());
				object.onCreated();
				object.getQuestFile().refreshIDMap();
				object.getQuestFile().clearCachedData();
				object.getQuestFile().markDirty();

				NetworkHelper.sendToAll(sp.getServer(), CreateObjectResponseMessage.create(object, message.extra.orElse(null), message.openScreen ? sp.getUUID() : null));
			}
		});
	}
}