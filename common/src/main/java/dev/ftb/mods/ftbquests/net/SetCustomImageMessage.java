package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftbquests.item.CustomIconItem;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

/**
 * @author LatvianModder
 */
public class SetCustomImageMessage extends BaseC2SMessage {
	private final InteractionHand hand;
	private final ResourceLocation texture;

	SetCustomImageMessage(FriendlyByteBuf buffer) {
		hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		texture = buffer.readResourceLocation();
	}

	public SetCustomImageMessage(InteractionHand h, ResourceLocation t) {
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
		buffer.writeResourceLocation(texture);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		if (context.getPlayer().getItemInHand(hand).getItem() instanceof CustomIconItem) {
			if (texture.equals(ImageResourceConfig.NONE)) {
				context.getPlayer().getItemInHand(hand).removeTagKey("Icon");
			} else {
				context.getPlayer().getItemInHand(hand).addTagElement("Icon", StringTag.valueOf(texture.toString()));
			}
		}
	}
}