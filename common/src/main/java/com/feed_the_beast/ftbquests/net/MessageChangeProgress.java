package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageChangeProgress extends MessageBase {
	private final UUID team;
	private final long id;
	private final ChangeProgress type;

	MessageChangeProgress(FriendlyByteBuf buffer) {
		team = NetUtils.readUUID(buffer);
		id = buffer.readLong();
		type = ChangeProgress.NAME_MAP.read(buffer);

	}

	public MessageChangeProgress(UUID t, long i, ChangeProgress ty) {
		team = t;
		id = i;
		type = ty;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		NetUtils.writeUUID(buffer, team);
		buffer.writeLong(id);
		ChangeProgress.NAME_MAP.write(buffer, type);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		if (NetUtils.canEdit(context)) {
			QuestObjectBase object = ServerQuestFile.INSTANCE.getBase(id);

			if (object != null) {
				PlayerData t = ServerQuestFile.INSTANCE.getData(team);
				object.forceProgress(t, type, false);
			}
		}
	}
}