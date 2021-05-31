package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseC2SPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class CreateTaskAtPacket extends BaseC2SPacket {
	private final long chapter;
	private final double x, y;
	private final TaskType type;
	private final CompoundTag nbt;

	CreateTaskAtPacket(FriendlyByteBuf buffer) {
		chapter = buffer.readLong();
		x = buffer.readDouble();
		y = buffer.readDouble();
		type = FTBQuests.PROXY.getClientQuestFile().taskTypeIds.get(buffer.readVarInt());
		nbt = buffer.readNbt();
	}

	public CreateTaskAtPacket(Chapter c, double _x, double _y, Task task) {
		chapter = c.id;
		x = _x;
		y = _y;
		type = task.getType();
		nbt = new CompoundTag();
		task.writeData(nbt);
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.CREATE_TASK_AT;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(chapter);
		buffer.writeDouble(x);
		buffer.writeDouble(y);
		buffer.writeVarInt(type.intId);
		buffer.writeNbt(nbt);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		if (NetUtils.canEdit(context)) {
			Chapter c = ServerQuestFile.INSTANCE.getChapter(chapter);

			if (c != null) {
				Quest quest = new Quest(c);
				quest.x = x;
				quest.y = y;
				quest.id = ServerQuestFile.INSTANCE.newID();
				quest.onCreated();
				new CreateObjectResponsePacket(quest, null).sendToAll(context.getPlayer().getServer());

				Task task = type.provider.create(quest);
				task.id = ServerQuestFile.INSTANCE.newID();
				task.readData(nbt);
				task.onCreated();
				CompoundTag extra = new CompoundTag();
				extra.putString("type", type.getTypeForNBT());
				new CreateObjectResponsePacket(task, extra).sendToAll(context.getPlayer().getServer());

				ServerQuestFile.INSTANCE.refreshIDMap();
				ServerQuestFile.INSTANCE.clearCachedData();
				ServerQuestFile.INSTANCE.save();
			}
		}
	}
}