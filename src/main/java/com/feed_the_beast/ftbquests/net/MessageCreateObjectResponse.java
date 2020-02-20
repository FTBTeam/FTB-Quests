package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class MessageCreateObjectResponse extends MessageBase
{
	private int id;
	private int parent;
	private QuestObjectType type;
	private CompoundNBT nbt;
	private CompoundNBT extra;

	public MessageCreateObjectResponse(PacketBuffer buffer)
	{
		id = buffer.readVarInt();
		parent = buffer.readVarInt();
		type = QuestObjectType.NAME_MAP.read(buffer);
		nbt = buffer.readCompoundTag();
		extra = buffer.readCompoundTag();
	}

	public MessageCreateObjectResponse(QuestObjectBase o, @Nullable CompoundNBT e)
	{
		id = o.id;
		parent = o.getParentID();
		type = o.getObjectType();
		nbt = new CompoundNBT();
		o.writeData(nbt);
		extra = e;
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		buffer.writeVarInt(id);
		buffer.writeVarInt(parent);
		QuestObjectType.NAME_MAP.write(buffer, type);
		buffer.writeCompoundTag(nbt);
		buffer.writeCompoundTag(extra);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		FTBQuests.NET_PROXY.createObject(id, parent, type, nbt, extra);
	}
}