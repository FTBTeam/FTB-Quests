package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.ItemHandlerHelper;

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
	public void handle(NetworkEvent.Context context)
	{
		//TODO: Verify on server side
		ServerPlayer player = context.getSender();

		for (ItemStack stack : ServerQuestFile.INSTANCE.emergencyItems)
		{
			ItemHandlerHelper.giveItemToPlayer(player, stack.copy());
		}
	}
}