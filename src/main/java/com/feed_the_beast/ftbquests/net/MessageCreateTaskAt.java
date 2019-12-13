package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.feed_the_beast.ftbquests.util.NetUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageCreateTaskAt extends MessageBase
{
	private final int chapter;
	private final double x, y;
	private final TaskType type;
	private final CompoundNBT nbt;

	MessageCreateTaskAt(PacketBuffer buffer)
	{
		chapter = buffer.readVarInt();
		x = buffer.readDouble();
		y = buffer.readDouble();
		type = TaskType.getRegistry().getValue(buffer.readVarInt());
		nbt = buffer.readCompoundTag();
	}

	public MessageCreateTaskAt(Chapter c, double _x, double _y, Task task)
	{
		chapter = c.id;
		x = _x;
		y = _y;
		type = task.getType();
		nbt = new CompoundNBT();
		task.writeData(nbt);
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		buffer.writeVarInt(chapter);
		buffer.writeDouble(x);
		buffer.writeDouble(y);
		buffer.writeVarInt(TaskType.getRegistry().getID(type));
		buffer.writeCompoundTag(nbt);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		if (NetUtils.canEdit(context))
		{
			Chapter c = ServerQuestFile.INSTANCE.getChapter(chapter);

			if (c != null)
			{
				Quest quest = new Quest(c);
				quest.x = x;
				quest.y = y;
				quest.id = ServerQuestFile.INSTANCE.newID();
				quest.onCreated();
				new MessageCreateObjectResponse(quest, null).sendToAll();

				Task task = type.provider.create(quest);
				task.id = ServerQuestFile.INSTANCE.newID();
				task.readData(nbt);
				task.onCreated();
				CompoundNBT extra = new CompoundNBT();
				extra.putString("type", type.getTypeForNBT());
				new MessageCreateObjectResponse(task, extra).sendToAll();

				ServerQuestFile.INSTANCE.refreshIDMap();
				ServerQuestFile.INSTANCE.clearCachedData();
				ServerQuestFile.INSTANCE.save();
			}
		}
	}
}