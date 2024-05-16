package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record EditObjectMessage(long id, CompoundTag nbt) implements CustomPacketPayload {
	public static final Type<EditObjectMessage> TYPE = new Type<>(FTBQuestsAPI.rl("edit_object_message"));

	public static final StreamCodec<FriendlyByteBuf, EditObjectMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, EditObjectMessage::id,
			ByteBufCodecs.COMPOUND_TAG, EditObjectMessage::nbt,
			EditObjectMessage::new
	);

	public static EditObjectMessage forQuestObject(QuestObjectBase qo) {
		FTBQuests.getRecipeModHelper().refreshRecipes(qo);
		ClientQuestFile.INSTANCE.clearCachedData();

		return new EditObjectMessage(qo.id, Util.make(new CompoundTag(), nbt1 -> qo.writeData(nbt1, ClientQuestFile.INSTANCE.holderLookup())));
	}

	public static void sendToServer(QuestObjectBase qo) {
		NetworkManager.sendToServer(forQuestObject(qo));
	}

	@Override
	public Type<EditObjectMessage> type() {
		return TYPE;
	}

	public static void handle(EditObjectMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			if (NetUtils.canEdit(context)) {
				QuestObjectBase object = ServerQuestFile.INSTANCE.getBase(message.id);
				if (object != null) {
					object.readData(message.nbt, context.registryAccess());
					object.editedFromGUIOnServer();
					ServerQuestFile.INSTANCE.clearCachedData();
					ServerQuestFile.INSTANCE.markDirty();
					NetworkHelper.sendToAll(context.getPlayer().getServer(), new EditObjectResponseMessage(object));
				}
			}
		});
	}
}