package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.quest.ChangeProgress;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.util.NetUtils;
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
		team = buffer.readUUID();
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
		buffer.writeUUID(team);
		buffer.writeLong(id);
		ChangeProgress.NAME_MAP.write(buffer, type);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		if (NetUtils.canEdit(context)) {
			QuestObjectBase object = ServerQuestFile.INSTANCE.getBase(id);

			if (object != null) {
				TeamData t = ServerQuestFile.INSTANCE.getData(team);
				object.forceProgress(t, type, false);
			}
		}
	}
}