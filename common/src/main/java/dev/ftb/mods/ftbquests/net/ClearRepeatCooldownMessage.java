package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record ClearRepeatCooldownMessage(long id) implements CustomPacketPayload {
	public static final Type<ClearRepeatCooldownMessage> TYPE = new Type<>(FTBQuestsAPI.id("clear_repeat_cooldown"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClearRepeatCooldownMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, ClearRepeatCooldownMessage::id,
			ClearRepeatCooldownMessage::new
	);

	@Override
	public Type<ClearRepeatCooldownMessage> type() {
		return TYPE;
	}

	public static void handle(ClearRepeatCooldownMessage message, PacketContext ignoredContext) {
		if (ClientQuestFile.exists() && ClientQuestFile.getInstance().getBase(message.id) instanceof Quest quest) {
			FTBQuestsClient.getClientPlayerData().clearRepeatCooldown(quest);
		}
	}

	public static void sendToTeam(Quest quest, UUID teamId) {
		FTBTeamsAPI.api().getManager().getTeamByID(teamId).ifPresent(team ->
				Server2PlayNetworking.send(team.getOnlineMembers(), new ClearRepeatCooldownMessage(quest.getId())));
	}
}
