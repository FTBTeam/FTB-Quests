package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author LatvianModder
 */
public class MessageCreateTaskAt extends MessageToServer
{
	private int chapter, x, y;
	private TaskType type;
	private NBTTagCompound nbt;

	public MessageCreateTaskAt()
	{
	}

	public MessageCreateTaskAt(Chapter c, int _x, int _y, Task task)
	{
		chapter = c.id;
		x = _x;
		y = _y;
		type = task.getType();
		nbt = new NBTTagCompound();
		task.writeData(nbt);
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeInt(chapter);
		data.writeVarInt(x);
		data.writeVarInt(y);
		data.writeVarInt(TaskType.getRegistry().getID(type));
		data.writeNBT(nbt);
	}

	@Override
	public void readData(DataIn data)
	{
		chapter = data.readInt();
		x = data.readVarInt();
		y = data.readVarInt();
		type = TaskType.getRegistry().getValue(data.readVarInt());
		nbt = data.readNBT();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuests.canEdit(player))
		{
			Chapter c = ServerQuestFile.INSTANCE.getChapter(chapter);

			if (c != null)
			{
				Quest quest = new Quest(c);
				quest.x = (byte) x;
				quest.y = (byte) y;
				quest.id = ServerQuestFile.INSTANCE.readID(0);
				quest.onCreated();
				new MessageCreateObjectResponse(quest, null).sendToAll();

				Task task = type.provider.create(quest);
				task.id = ServerQuestFile.INSTANCE.readID(0);
				task.readData(nbt);
				task.onCreated();
				NBTTagCompound extra = new NBTTagCompound();
				extra.setString("type", type.getTypeForNBT());
				new MessageCreateObjectResponse(task, extra).sendToAll();

				ServerQuestFile.INSTANCE.refreshIDMap();
				ServerQuestFile.INSTANCE.clearCachedData();
				ServerQuestFile.INSTANCE.save();
			}
		}
	}
}