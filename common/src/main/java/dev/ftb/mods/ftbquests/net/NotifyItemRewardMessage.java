package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record NotifyItemRewardMessage(ItemStack stack, int count, boolean disableBlur) implements CustomPacketPayload {
	public static final Type<NotifyItemRewardMessage> TYPE = new Type<>(FTBQuestsAPI.id("notify_item_reward_message"));

	public static final StreamCodec<RegistryFriendlyByteBuf, NotifyItemRewardMessage> STREAM_CODEC = StreamCodec.composite(
			ItemStack.OPTIONAL_STREAM_CODEC, NotifyItemRewardMessage::stack,
			ByteBufCodecs.VAR_INT, NotifyItemRewardMessage::count,
			ByteBufCodecs.BOOL, NotifyItemRewardMessage::disableBlur,
			NotifyItemRewardMessage::new
	);

	@Override
	public Type<NotifyItemRewardMessage> type() {
		return TYPE;
	}

	public static void handle(NotifyItemRewardMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.displayItemRewardToast(message.stack, message.count, message.disableBlur));
	}
}
