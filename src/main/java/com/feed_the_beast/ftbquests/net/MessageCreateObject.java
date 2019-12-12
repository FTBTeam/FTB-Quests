package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class MessageCreateObject extends MessageBase
{
	private final int parent;
	private final QuestObjectType type;
	private final CompoundNBT nbt;
	private final CompoundNBT extra;

	MessageCreateObject(PacketBuffer buffer)
	{
		parent = buffer.readInt();
		type = QuestObjectType.NAME_MAP.read(buffer);
		nbt = buffer.readCompoundTag();
		extra = buffer.readCompoundTag();
	}

	public MessageCreateObject(QuestObjectBase o, @Nullable CompoundNBT e)
	{
		parent = o.getParentID();
		type = o.getObjectType();
		nbt = new CompoundNBT();
		o.writeData(nbt);
		extra = e;
	}

	public void write(PacketBuffer buffer)
	{
		buffer.writeInt(parent);
		QuestObjectType.NAME_MAP.write(buffer, type);
		buffer.writeCompoundTag(nbt);
		buffer.writeCompoundTag(extra);
	}

	public void handle(NetworkEvent.Context context)
	{
		if (NetUtils.canEdit(context))
		{
			QuestObjectBase object = ServerQuestFile.INSTANCE.create(type, parent, extra == null ? new CompoundNBT() : extra);
			object.readData(nbt);
			object.id = ServerQuestFile.INSTANCE.newID();
			object.onCreated();
			ServerQuestFile.INSTANCE.refreshIDMap();
			ServerQuestFile.INSTANCE.clearCachedData();
			ServerQuestFile.INSTANCE.save();
			new MessageCreateObjectResponse(object, extra).sendToAll();
		}
	}
}