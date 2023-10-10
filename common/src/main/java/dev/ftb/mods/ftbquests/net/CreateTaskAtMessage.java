package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class CreateTaskAtMessage extends BaseC2SMessage {
	private final long chapterId;
	private final double x, y;
	private final TaskType type;
	private final CompoundTag nbt;

	public CreateTaskAtMessage(Chapter chapter, double x, double y, Task task) {
		chapterId = chapter.id;
		this.x = x;
		this.y = y;
		type = task.getType();
		nbt = new CompoundTag();
		task.writeData(nbt);
	}

	CreateTaskAtMessage(FriendlyByteBuf buffer) {
		chapterId = buffer.readLong();
		x = buffer.readDouble();
		y = buffer.readDouble();
		type = ServerQuestFile.INSTANCE.getTaskType(buffer.readVarInt()); //taskTypeIds.get(buffer.readVarInt());
		nbt = buffer.readNbt();
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.CREATE_TASK_AT;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(chapterId);
		buffer.writeDouble(x);
		buffer.writeDouble(y);
		buffer.writeVarInt(type.internalId);
		buffer.writeNbt(nbt);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		if (NetUtils.canEdit(context) && context.getPlayer() instanceof ServerPlayer sp) {
			ServerQuestFile file = ServerQuestFile.INSTANCE;
			Chapter ch = file.getChapter(chapterId);

			if (ch != null) {
				Quest quest = new Quest(file.newID(), ch);
				quest.setX(x);
				quest.setY(y);
				quest.onCreated();
				new CreateObjectResponseMessage(quest, null).sendToAll(sp.getServer());

				Task task = type.createTask(file.newID(), quest);
				task.readData(nbt);
				task.onCreated();
				CompoundTag extra = new CompoundTag();
				extra.putString("type", type.getTypeForNBT());
				new CreateObjectResponseMessage(task, extra, sp.getUUID()).sendToAll(sp.getServer());

				file.refreshIDMap();
				file.clearCachedData();
				file.markDirty();
			}
		}
	}
}