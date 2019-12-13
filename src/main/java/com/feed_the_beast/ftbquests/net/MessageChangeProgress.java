package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageChangeProgress extends MessageBase
{
	private final UUID team;
	private final int id;
	private final ChangeProgress type;

	MessageChangeProgress(PacketBuffer buffer)
	{
		team = NetUtils.readUUID(buffer);
		id = buffer.readVarInt();
		type = ChangeProgress.NAME_MAP.read(buffer);

	}

	public MessageChangeProgress(UUID t, int i, ChangeProgress ty)
	{
		team = t;
		id = i;
		type = ty;
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		NetUtils.writeUUID(buffer, team);
		buffer.writeVarInt(id);
		ChangeProgress.NAME_MAP.write(buffer, type);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		if (NetUtils.canEdit(context))
		{
			QuestObjectBase object = ServerQuestFile.INSTANCE.getBase(id);

			if (object != null)
			{
				PlayerData t = ServerQuestFile.INSTANCE.getData(team);
				object.forceProgress(t, type, false);
			}
		}
	}
}