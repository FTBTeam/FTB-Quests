package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class MessageCreateObjectResponse extends MessageBase
{
	private int id;
	private int parent;
	private QuestObjectType type;
	private CompoundTag nbt;
	private CompoundTag extra;

	public MessageCreateObjectResponse(FriendlyByteBuf buffer)
	{
		id = buffer.readVarInt();
		parent = buffer.readVarInt();
		type = QuestObjectType.NAME_MAP.read(buffer);
		nbt = buffer.readNbt();
		extra = buffer.readNbt();
	}

	public MessageCreateObjectResponse(QuestObjectBase o, @Nullable CompoundTag e)
	{
		id = o.id;
		parent = o.getParentID();
		type = o.getObjectType();
		nbt = new CompoundTag();
		o.writeData(nbt);
		extra = e;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeVarInt(id);
		buffer.writeVarInt(parent);
		QuestObjectType.NAME_MAP.write(buffer, type);
		buffer.writeNbt(nbt);
		buffer.writeNbt(extra);
	}

	@Override
	public void handle(NetworkManager.PacketContext context)
	{
		FTBQuests.NET_PROXY.createObject(id, parent, type, nbt, extra);
	}
}