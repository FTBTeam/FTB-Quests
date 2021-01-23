package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MessageEditObjectResponse extends MessageBase
{
	private final int id;
	private final CompoundTag nbt;

	MessageEditObjectResponse(FriendlyByteBuf buffer)
	{
		id = buffer.readVarInt();
		nbt = buffer.readNbt();
	}

	public MessageEditObjectResponse(QuestObjectBase o)
	{
		id = o.id;
		nbt = new CompoundTag();
		o.writeData(nbt);
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeVarInt(id);
		buffer.writeNbt(nbt);
	}

	@Override
	public void handle(NetworkManager.PacketContext context)
	{
		FTBQuests.NET_PROXY.editObject(id, nbt);
	}
}