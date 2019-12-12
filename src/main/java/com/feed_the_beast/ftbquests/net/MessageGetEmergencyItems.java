package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * @author LatvianModder
 */
public class MessageGetEmergencyItems extends MessageBase
{
	MessageGetEmergencyItems(PacketBuffer buffer)
	{
	}

	public MessageGetEmergencyItems()
	{
	}

	public void write(PacketBuffer buffer)
	{
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		//TODO: Verify on server side
		ServerPlayerEntity player = context.getSender();

		for (ItemStack stack : ServerQuestFile.INSTANCE.emergencyItems)
		{
			ItemHandlerHelper.giveItemToPlayer(player, stack.copy());
		}
	}
}