package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.item.CustomIconItem;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public class SetCustomImageMessage extends BaseC2SMessage {
	private final InteractionHand hand;
	private final String texture;

	SetCustomImageMessage(FriendlyByteBuf buffer) {
		hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		texture = buffer.readUtf(Short.MAX_VALUE);
	}

	public SetCustomImageMessage(InteractionHand h, String t) {
		hand = h;
		texture = t;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.SET_CUSTOM_IMAGE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(hand == InteractionHand.MAIN_HAND);
		buffer.writeUtf(texture, Short.MAX_VALUE);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		if (context.getPlayer().getItemInHand(hand).getItem() instanceof CustomIconItem) {
			if (texture.isEmpty()) {
				context.getPlayer().getItemInHand(hand).removeTagKey("Icon");
			} else {
				context.getPlayer().getItemInHand(hand).addTagElement("Icon", StringTag.valueOf(texture));
			}
		}
	}
}
