package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageEditObject extends MessageBase
{
	private final int id;
	private final CompoundNBT nbt;

	MessageEditObject(PacketBuffer buffer)
	{
		id = buffer.readInt();
		nbt = buffer.readCompoundTag();
	}

	public MessageEditObject(QuestObjectBase o)
	{
		id = o.id;
		nbt = new CompoundNBT();
		o.writeData(nbt);
		FTBQuestsJEIHelper.refresh(o);
		ClientQuestFile.INSTANCE.clearCachedData();
		o.editedFromGUI();
	}

	public void write(PacketBuffer buffer)
	{
		buffer.writeInt(id);
		buffer.writeCompoundTag(nbt);
	}

	public void handle(NetworkEvent.Context context)
	{
		if (NetUtils.canEdit(context))
		{
			QuestObjectBase object = ServerQuestFile.INSTANCE.getBase(id);

			if (object != null)
			{
				object.readData(nbt);
				ServerQuestFile.INSTANCE.clearCachedData();
				ServerQuestFile.INSTANCE.save();
				new MessageEditObjectResponse(object).sendToAll();
			}
		}
	}
}