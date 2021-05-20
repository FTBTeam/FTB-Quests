package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseC2SPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import me.shedaniel.architectury.hooks.ItemStackHooks;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class GetEmergencyItemsPacket extends BaseC2SPacket {
	GetEmergencyItemsPacket(FriendlyByteBuf buffer) {
	}

	public GetEmergencyItemsPacket() {
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.GET_EMERGENCY_ITEMS;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		//TODO: Verify on server side
		ServerPlayer player = (ServerPlayer) context.getPlayer();

		for (ItemStack stack : ServerQuestFile.INSTANCE.emergencyItems) {
			ItemStackHooks.giveItem(player, stack.copy());
		}
	}
}