package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncQuestsMessage(BaseQuestFile file) implements CustomPacketPayload {
	public static final Type<SyncQuestsMessage> TYPE = new Type<>(FTBQuestsAPI.id("sync_quests_message"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SyncQuestsMessage> STREAM_CODEC = StreamCodec.composite(
			BaseQuestFile.STREAM_CODEC, SyncQuestsMessage::file,
			SyncQuestsMessage::new
	);

	@Override
	public Type<SyncQuestsMessage> type() {
		return TYPE;
	}

	public static void handle(SyncQuestsMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			ClientQuestFile.syncFromServer(message.file);

			ClientQuestFile.INSTANCE.updateLootCrates();

			NetworkManager.sendToServer(RequestTeamDataMessage.INSTANCE);
		});
	}
}
