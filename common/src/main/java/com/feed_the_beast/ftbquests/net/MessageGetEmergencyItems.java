package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import me.shedaniel.architectury.hooks.ItemStackHooks;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class MessageGetEmergencyItems extends MessageBase
{
	MessageGetEmergencyItems(FriendlyByteBuf buffer)
	{
	}

	public MessageGetEmergencyItems()
	{
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
	}

	@Override
	public void handlePacket(NetworkManager.PacketContext context)
	{
		//TODO: Verify on server side
		ServerPlayer player = (ServerPlayer) context.getPlayer();

		for (ItemStack stack : ServerQuestFile.INSTANCE.emergencyItems)
		{
			ItemStackHooks.giveItem(player, stack.copy());
		}
	}
}