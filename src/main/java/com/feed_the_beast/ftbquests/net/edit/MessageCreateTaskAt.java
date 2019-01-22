package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author LatvianModder
 */
public class MessageCreateTaskAt extends MessageToServer
{
	private int chapter, x, y;
	private QuestTaskType type;
	private NBTTagCompound nbt;

	public MessageCreateTaskAt()
	{
	}

	public MessageCreateTaskAt(QuestChapter c, int _x, int _y, QuestTask task)
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
		data.writeVarInt(QuestTaskType.getRegistry().getID(type));
		data.writeNBT(nbt);
	}

	@Override
	public void readData(DataIn data)
	{
		chapter = data.readInt();
		x = data.readVarInt();
		y = data.readVarInt();
		type = QuestTaskType.getRegistry().getValue(data.readVarInt());
		nbt = data.readNBT();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuests.canEdit(player))
		{
			QuestChapter c = ServerQuestFile.INSTANCE.getChapter(chapter);

			if (c != null)
			{
				Quest quest = new Quest(c);
				quest.x = (byte) x;
				quest.y = (byte) y;
				quest.id = ServerQuestFile.INSTANCE.readID(0);
				quest.onCreated();
				new MessageCreateObjectResponse(quest, null).sendToAll();

				QuestTask task = type.provider.create(quest);
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