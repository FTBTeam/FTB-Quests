package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageEditObjectResponse extends MessageBase
{
	private final int id;
	private final CompoundNBT nbt;

	MessageEditObjectResponse(PacketBuffer buffer)
	{
		id = buffer.readVarInt();
		nbt = buffer.readCompoundTag();
	}

	public MessageEditObjectResponse(QuestObjectBase o)
	{
		id = o.id;
		nbt = new CompoundNBT();
		o.writeData(nbt);
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		buffer.writeVarInt(id);
		buffer.writeCompoundTag(nbt);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		ClientQuestFile.INSTANCE.clearCachedData();
		QuestObjectBase object = ClientQuestFile.INSTANCE.getBase(id);

		if (object != null)
		{
			object.readData(nbt);
			object.editedFromGUI();
			FTBQuestsJEIHelper.refresh(object);
		}
	}
}