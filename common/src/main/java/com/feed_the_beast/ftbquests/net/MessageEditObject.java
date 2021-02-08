package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MessageEditObject extends MessageBase
{
	private final long id;
	private final CompoundTag nbt;

	MessageEditObject(FriendlyByteBuf buffer)
	{
		id = buffer.readLong();
		nbt = buffer.readNbt();
	}

	public MessageEditObject(QuestObjectBase o)
	{
		id = o.id;
		nbt = new CompoundTag();
		o.writeData(nbt);
		FTBQuestsJEIHelper.refresh(o);
		ClientQuestFile.INSTANCE.clearCachedData();
		o.editedFromGUI();
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeLong(id);
		buffer.writeNbt(nbt);
	}

	@Override
	public void handle(NetworkManager.PacketContext context)
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