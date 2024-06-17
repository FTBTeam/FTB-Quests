package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.item.CustomIconItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public record SetCustomImageMessage(InteractionHand hand, ResourceLocation texture) implements CustomPacketPayload {
	public static final Type<SetCustomImageMessage> TYPE = new Type<>(FTBQuestsAPI.rl("set_custom_image_message"));

	public static final StreamCodec<FriendlyByteBuf, SetCustomImageMessage> STREAM_CODEC = StreamCodec.composite(
			NetworkHelper.enumStreamCodec(InteractionHand.class), SetCustomImageMessage::hand,
			ResourceLocation.STREAM_CODEC, SetCustomImageMessage::texture,
			SetCustomImageMessage::new
	);

	@Override
	public Type<SetCustomImageMessage> type() {
		return TYPE;
	}

	public static void handle(SetCustomImageMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			if (context.getPlayer().getItemInHand(message.hand).getItem() instanceof CustomIconItem) {
				CustomIconItem.setIcon(context.getPlayer().getItemInHand(message.hand), message.texture);
			}
		});
	}
}