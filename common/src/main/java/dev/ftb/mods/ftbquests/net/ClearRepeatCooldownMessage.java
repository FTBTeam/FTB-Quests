package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;

public record ClearRepeatCooldownMessage(long id) implements CustomPacketPayload {
	public static final Type<ClearRepeatCooldownMessage> TYPE = new Type<>(FTBQuestsAPI.rl("clear_repeat_cooldown"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClearRepeatCooldownMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, ClearRepeatCooldownMessage::id,
			ClearRepeatCooldownMessage::new
	);

	@Override
	public Type<ClearRepeatCooldownMessage> type() {
		return TYPE;
	}

	public static void handle(ClearRepeatCooldownMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			if (ClientQuestFile.exists() && ClientQuestFile.INSTANCE.getBase(message.id) instanceof Quest quest) {
				ClientQuestFile.INSTANCE.selfTeamData.clearRepeatCooldown(quest);
			}
		});
	}

	public static void sendToAll(MinecraftServer server, Quest quest) {
		NetworkHelper.sendToAll(server, new ClearRepeatCooldownMessage(quest.getId()));
	}
}