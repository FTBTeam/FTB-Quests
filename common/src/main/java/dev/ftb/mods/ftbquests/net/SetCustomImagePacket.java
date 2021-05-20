package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseC2SPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.item.CustomIconItem;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

/**
 * @author LatvianModder
 */
public class SetCustomImagePacket extends BaseC2SPacket {
	private final InteractionHand hand;
	private final String texture;

	SetCustomImagePacket(FriendlyByteBuf buffer) {
		hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		texture = buffer.readUtf(Short.MAX_VALUE);
	}

	public SetCustomImagePacket(InteractionHand h, String t) {
		hand = h;
		texture = t;
	}

	@Override
	public PacketID getId() {
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