package dev.ftb.mods.ftbquests.net;

import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5NetPacker;
import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;

public record EditObjectMessage(long id, Json5Element json) implements CustomPacketPayload {
	public static final Type<EditObjectMessage> TYPE = new Type<>(FTBQuestsAPI.id("edit_object_message"));

	public static final StreamCodec<FriendlyByteBuf, EditObjectMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, EditObjectMessage::id,
			Json5NetPacker.CODEC, EditObjectMessage::json,
			EditObjectMessage::new
	);

	public static EditObjectMessage forQuestObject(QuestObjectBase qo) {
		FTBQuests.getRecipeModHelper().refreshRecipes(qo);
		ClientQuestFile.getInstance().clearCachedData();

		return new EditObjectMessage(qo.id, Util.make(new Json5Object(), o -> qo.writeData(o, ClientQuestFile.getInstance().holderLookup())));
	}

	public static void sendToServer(QuestObjectBase qo) {
		Play2ServerNetworking.send(forQuestObject(qo));
	}

	@Override
	public Type<EditObjectMessage> type() {
		return TYPE;
	}

	public static void handle(EditObjectMessage message, PacketContext context) {
		if (NetUtils.canEdit(context) && message.json instanceof Json5Object json5Object && context.player().level() instanceof ServerLevel level) {
			QuestObjectBase object = ServerQuestFile.getInstance().getBase(message.id);
			if (object != null) {
				object.readData(json5Object, level.registryAccess());
				ServerQuestFile.getInstance().clearCachedData();
				ServerQuestFile.getInstance().markDirty();
				Server2PlayNetworking.sendToAllPlayers(level.getServer(), new EditObjectResponseMessage(object));
				object.editedFromGUIOnServer();
			}
		}
	}
}
