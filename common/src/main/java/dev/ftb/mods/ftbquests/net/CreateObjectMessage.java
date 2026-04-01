package dev.ftb.mods.ftbquests.net;

import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5NetPacker;
import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Received on: SERVER<br>
 * Sent by client to create a brand-new quest object of any kind
 *
 * @param parent id of the parent object (the quest id for tasks/chapters, the chapter id for quests/quest links,
 *              and 1 for everything else)
 * @param questObjectType type of the new object
 * @param openScreen true if the quest book should be opened after the object is created
 * @param nbt the serialized quest data
 * @param extra extra data related to the object type (e.g. task type, reward type, chapter group...)
 */
public record CreateObjectMessage(long parent, QuestObjectType questObjectType, boolean openScreen, Json5Element nbt, Optional<Json5Element> extra) implements CustomPacketPayload {
	public static final Type<CreateObjectMessage> TYPE = new Type<>(FTBQuestsAPI.id("create_object_message"));

	public static final StreamCodec<FriendlyByteBuf, CreateObjectMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, CreateObjectMessage::parent,
			QuestObjectType.STREAM_CODEC, CreateObjectMessage::questObjectType,
			ByteBufCodecs.BOOL, CreateObjectMessage::openScreen,
			Json5NetPacker.CODEC, CreateObjectMessage::nbt,
			ByteBufCodecs.optional(Json5NetPacker.CODEC), CreateObjectMessage::extra,
			CreateObjectMessage::new
	);

	public static CreateObjectMessage create(QuestObjectBase questObject, @Nullable Json5Element extra, boolean openScreen) {
		return new CreateObjectMessage(questObject.getParentID(),
				questObject.getObjectType(),
				openScreen,
				Util.make(new Json5Object(), nbt1 -> questObject.writeData(nbt1, questObject.getQuestFile().holderLookup())),
				Optional.ofNullable(extra)
		);
	}

	public static CreateObjectMessage create(QuestObjectBase questObject, @Nullable Json5Element extra) {
		return create(questObject, extra, true);
	}

	@Override
	public Type<CreateObjectMessage> type() {
		return TYPE;
	}

	public static void handle(CreateObjectMessage message, PacketContext context) {
			if (NetUtils.canEdit(context) && context.player() instanceof ServerPlayer sp && message.nbt instanceof Json5Object json) {
				Json5Object extra = message.extra
						.filter(e -> e instanceof Json5Object)
						.map(Json5Element::getAsJson5Object)
						.orElseGet(Json5Object::new);

				QuestObjectBase object = ServerQuestFile.getInstance().create(
						ServerQuestFile.getInstance().newID(), message.questObjectType, message.parent, extra
				);
				object.readData(json, context.player().registryAccess());

				object.onCreated();
				object.getQuestFile().refreshIDMap();
				object.getQuestFile().clearCachedData();
				object.getQuestFile().markDirty();

				object.getQuestFile().getTranslationManager().processInitialTranslation(extra, object);

				Server2PlayNetworking.sendToAllPlayers(sp.level().getServer(), CreateObjectResponseMessage.create(object, message.extra.orElse(null), message.openScreen ? sp.getUUID() : null));
			}
	}
}
