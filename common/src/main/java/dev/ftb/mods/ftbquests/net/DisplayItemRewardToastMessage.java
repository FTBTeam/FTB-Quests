package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record DisplayItemRewardToastMessage(ItemStack stack, int count, boolean disableBlur) implements CustomPacketPayload {
	public static final Type<DisplayItemRewardToastMessage> TYPE = new Type<>(FTBQuestsAPI.rl("display_item_reward_toast_message"));

	public static final StreamCodec<RegistryFriendlyByteBuf, DisplayItemRewardToastMessage> STREAM_CODEC = StreamCodec.composite(
			ItemStack.OPTIONAL_STREAM_CODEC, DisplayItemRewardToastMessage::stack,
			ByteBufCodecs.VAR_INT, DisplayItemRewardToastMessage::count,
			ByteBufCodecs.BOOL, DisplayItemRewardToastMessage::disableBlur,
			DisplayItemRewardToastMessage::new
	);

	@Override
	public Type<DisplayItemRewardToastMessage> type() {
		return TYPE;
	}

	public static void handle(DisplayItemRewardToastMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.displayItemRewardToast(message.stack, message.count, message.disableBlur));
	}
}