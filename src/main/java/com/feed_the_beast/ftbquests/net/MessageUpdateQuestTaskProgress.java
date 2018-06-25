package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.ClientQuestList;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskKey;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageUpdateQuestTaskProgress extends MessageToClient
{
	private QuestTaskKey key;
	private int progress;

	public MessageUpdateQuestTaskProgress()
	{
	}

	public MessageUpdateQuestTaskProgress(QuestTaskKey k, int p)
	{
		key = k;
		progress = p;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		QuestTaskKey.SERIALIZER.write(data, key);
		data.writeInt(progress);
	}

	@Override
	public void readData(DataIn data)
	{
		key = QuestTaskKey.DESERIALIZER.read(data);
		progress = data.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		ClientQuestList.INSTANCE.setQuestTaskProgress(key, progress);
	}
}