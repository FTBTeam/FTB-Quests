package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.util.NetUtils;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import me.shedaniel.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public class MessageChangeProgress extends MessageBase {
	private final UUID team;
	private final ProgressChange progressChange;

	MessageChangeProgress(FriendlyByteBuf buffer) {
		team = buffer.readUUID();
		progressChange = new ProgressChange(ServerQuestFile.INSTANCE, buffer);
	}

	public MessageChangeProgress(UUID t, ProgressChange p) {
		team = t;
		progressChange = p;
	}

	@Environment(EnvType.CLIENT)
	public static void send(TeamData team, QuestObjectBase object, Consumer<ProgressChange> progressChange) {
		if (team.isLocked()) {
			return;
		}

		ProgressChange change = new ProgressChange(team.file);
		change.origin = object;
		change.player = Minecraft.getInstance().player.getUUID();
		progressChange.accept(change);
		new MessageChangeProgress(team.uuid, change).sendToServer();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(team);
		progressChange.write(buffer);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		if (NetUtils.canEdit(context)) {
			if (progressChange.origin != null) {
				TeamData t = ServerQuestFile.INSTANCE.getData(team);
				progressChange.origin.forceProgressRaw(t, progressChange);
			}
		}
	}
}