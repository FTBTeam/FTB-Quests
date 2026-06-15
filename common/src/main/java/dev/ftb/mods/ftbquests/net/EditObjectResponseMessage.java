package dev.ftb.mods.ftbquests.net;

import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5NetPacker;
import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Util;

public record EditObjectResponseMessage(long id, Json5Element json) implements CustomPacketPayload {
	public static final Type<EditObjectResponseMessage> TYPE = new Type<>(FTBQuestsAPI.id("edit_object_response_message"));

	public static final StreamCodec<FriendlyByteBuf, EditObjectResponseMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, EditObjectResponseMessage::id,
			Json5NetPacker.CODEC, EditObjectResponseMessage::json,
			EditObjectResponseMessage::new
	);

	public EditObjectResponseMessage(QuestObjectBase questObjectBase) {
		this(questObjectBase.id, Util.make(new Json5Object(), o -> questObjectBase.writeData(o, questObjectBase.getQuestFile().holderLookup())));
	}

	@Override
	public Type<EditObjectResponseMessage> type() {
		return TYPE;
	}

	public static void handle(EditObjectResponseMessage message, PacketContext ignoredContext) {
		if (message.json instanceof Json5Object o) {
			FTBQuestsNetClient.editObject(message.id, o);
		}
	}
}
