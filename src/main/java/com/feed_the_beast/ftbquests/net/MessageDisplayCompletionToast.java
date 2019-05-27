package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.tree.ToastQuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageDisplayCompletionToast extends MessageToClient
{
	public int id;

	public MessageDisplayCompletionToast()
	{
	}

	public MessageDisplayCompletionToast(int i)
	{
		id = i;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeInt(id);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.exists())
		{
			QuestObject object = ClientQuestFile.INSTANCE.get(id);

			if (object != null)
			{
				Minecraft.getMinecraft().getToastGui().add(new ToastQuestObject(object));
			}

			ClientQuestFile.INSTANCE.questTreeGui.questPanel.refreshWidgets();
			ClientQuestFile.INSTANCE.questTreeGui.chapterPanel.refreshWidgets();
			ClientQuestFile.INSTANCE.questTreeGui.viewQuestPanel.refreshWidgets();
		}
	}
}