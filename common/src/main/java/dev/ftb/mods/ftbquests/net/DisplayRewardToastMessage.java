package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DisplayRewardToastMessage(long id, Component text, Icon icon) implements CustomPacketPayload {
	public static final Type<DisplayRewardToastMessage> TYPE = new Type<>(FTBQuestsAPI.rl("display_reward_toast_message"));

	public static final StreamCodec<RegistryFriendlyByteBuf, DisplayRewardToastMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, DisplayRewardToastMessage::id,
			ComponentSerialization.STREAM_CODEC, DisplayRewardToastMessage::text,
			Icon.STREAM_CODEC, DisplayRewardToastMessage::icon,
			DisplayRewardToastMessage::new
	);

	@Override
	public Type<DisplayRewardToastMessage> type() {
		return TYPE;
	}

	public static void handle(DisplayRewardToastMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.displayRewardToast(message.id, message.text, message.icon));
	}
}