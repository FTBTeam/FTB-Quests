package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.item.CustomIconItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;

public record SetCustomImageMessage(InteractionHand hand, boolean isEntityFace, Identifier texture) implements CustomPacketPayload {
	public static final Type<SetCustomImageMessage> TYPE = new Type<>(FTBQuestsAPI.id("set_custom_image_message"));

	public static final StreamCodec<FriendlyByteBuf, SetCustomImageMessage> STREAM_CODEC = StreamCodec.composite(
			NetworkHelper.enumStreamCodec(InteractionHand.class), SetCustomImageMessage::hand,
			ByteBufCodecs.BOOL, SetCustomImageMessage::isEntityFace,
			Identifier.STREAM_CODEC, SetCustomImageMessage::texture,
			SetCustomImageMessage::new
	);

	@Override
	public Type<SetCustomImageMessage> type() {
		return TYPE;
	}

	public static void handle(SetCustomImageMessage message, PacketContext context) {
		if (context.player().getItemInHand(message.hand).getItem() instanceof CustomIconItem) {
			if (message.isEntityFace) {
				CustomIconItem.setFaceIcon(context.player().getItemInHand(message.hand), message.texture);
			} else {
				CustomIconItem.setIcon(context.player().getItemInHand(message.hand), message.texture);
			}
		}
	}
}
