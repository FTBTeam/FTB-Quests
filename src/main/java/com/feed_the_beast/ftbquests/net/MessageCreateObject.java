package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class MessageCreateObject extends MessageBase
{
	private final int parent;
	private final QuestObjectType type;
	private final CompoundTag nbt;
	private final CompoundTag extra;

	MessageCreateObject(FriendlyByteBuf buffer)
	{
		parent = buffer.readVarInt();
		type = QuestObjectType.NAME_MAP.read(buffer);
		nbt = buffer.readNbt();
		extra = buffer.readNbt();
	}

	public MessageCreateObject(QuestObjectBase o, @Nullable CompoundTag e)
	{
		parent = o.getParentID();
		type = o.getObjectType();
		nbt = new CompoundTag();
		o.writeData(nbt);
		extra = e;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeVarInt(parent);
		QuestObjectType.NAME_MAP.write(buffer, type);
		buffer.writeNbt(nbt);
		buffer.writeNbt(extra);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		if (NetUtils.canEdit(context))
		{
			QuestObjectBase object = ServerQuestFile.INSTANCE.create(type, parent, extra == null ? new CompoundTag() : extra);
			object.readData(nbt);
			object.id = ServerQuestFile.INSTANCE.newID();
			object.onCreated();
			object.getQuestFile().refreshIDMap();
			object.getQuestFile().clearCachedData();
			object.getQuestFile().save();

			if (object instanceof Chapter)
			{
				object.getQuestFile().updateChapterIndices();
			}

			new MessageCreateObjectResponse(object, extra).sendToAll();
		}
	}
}