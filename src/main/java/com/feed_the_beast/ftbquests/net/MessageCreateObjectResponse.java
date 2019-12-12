package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.quest.Chapter;
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
		id = buffer.readInt();
		parent = buffer.readInt();
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
		buffer.writeInt(id);
		buffer.writeInt(parent);
		QuestObjectType.NAME_MAP.write(buffer, type);
		buffer.writeCompoundTag(nbt);
		buffer.writeCompoundTag(extra);
	}

	public void handle(NetworkEvent.Context context)
	{
		QuestObjectBase object = ClientQuestFile.INSTANCE.create(type, parent, extra == null ? new CompoundNBT() : extra);
		object.readData(nbt);
		object.id = id;
		object.onCreated();
		ClientQuestFile.INSTANCE.refreshIDMap();
		object.editedFromGUI();
		FTBQuestsJEIHelper.refresh(object);

		if (object instanceof Chapter)
		{
			ClientQuestFile.INSTANCE.questTreeGui.selectChapter((Chapter) object);
		}
	}
}