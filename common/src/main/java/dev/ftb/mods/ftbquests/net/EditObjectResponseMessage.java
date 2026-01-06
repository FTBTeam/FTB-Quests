package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record EditObjectResponseMessage(long id, CompoundTag nbt) implements CustomPacketPayload {
	public static final Type<EditObjectResponseMessage> TYPE = new Type<>(FTBQuestsAPI.id("edit_object_response_message"));

	public static final StreamCodec<FriendlyByteBuf, EditObjectResponseMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, EditObjectResponseMessage::id,
			ByteBufCodecs.COMPOUND_TAG, EditObjectResponseMessage::nbt,
			EditObjectResponseMessage::new
	);

	public EditObjectResponseMessage(QuestObjectBase questObjectBase) {
		this(questObjectBase.id, Util.make(new CompoundTag(), nbt1 -> questObjectBase.writeData(nbt1, questObjectBase.getQuestFile().holderLookup())));
	}

	@Override
	public Type<EditObjectResponseMessage> type() {
		return TYPE;
	}

	public static void handle(EditObjectResponseMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.editObject(message.id, message.nbt));
	}
}
