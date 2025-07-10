package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CustomToastMessage(Component title, Component text, Icon icon) implements CustomPacketPayload {
	public static final Type<CustomToastMessage> TYPE = new Type<>(FTBQuestsAPI.rl("custom_toast_message"));

	public static final StreamCodec<RegistryFriendlyByteBuf, CustomToastMessage> STREAM_CODEC = StreamCodec.composite(
			ComponentSerialization.STREAM_CODEC, CustomToastMessage::title,
			ComponentSerialization.STREAM_CODEC, CustomToastMessage::text,
			Icon.STREAM_CODEC, CustomToastMessage::icon,
			CustomToastMessage::new
	);

	@Override
	public Type<CustomToastMessage> type() {
		return TYPE;
	}

	public static void handle(CustomToastMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.displayCustomToast(message.title, message.text, message.icon));
	}
}