package dev.ftb.mods.ftbquests.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;

public record NotifyRewardMessage(long id, Component text, Icon<?> icon, boolean disableBlur) implements CustomPacketPayload {
	public static final Type<NotifyRewardMessage> TYPE = new Type<>(FTBQuestsAPI.id("notify_reward_message"));

	public static final StreamCodec<RegistryFriendlyByteBuf, NotifyRewardMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, NotifyRewardMessage::id,
			ComponentSerialization.STREAM_CODEC, NotifyRewardMessage::text,
			Icon.STREAM_CODEC, NotifyRewardMessage::icon,
			ByteBufCodecs.BOOL, NotifyRewardMessage::disableBlur,
			NotifyRewardMessage::new
	);

	@Override
	public Type<NotifyRewardMessage> type() {
		return TYPE;
	}

	public static void handle(NotifyRewardMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.displayRewardToast(message.id, message.text, message.icon, message.disableBlur));
	}
}
