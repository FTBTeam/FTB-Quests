package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.ToastQuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageDisplayCompletionToast extends MessageBase
{
	private final int id;

	MessageDisplayCompletionToast(PacketBuffer buffer)
	{
		id = buffer.readVarInt();
	}

	public MessageDisplayCompletionToast(int i)
	{
		id = i;
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		buffer.writeVarInt(id);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		if (ClientQuestFile.exists())
		{
			QuestObject object = ClientQuestFile.INSTANCE.get(id);

			if (object != null)
			{
				Minecraft.getInstance().getToastGui().add(new ToastQuestObject(object));
			}

			ClientQuestFile.INSTANCE.questTreeGui.questPanel.refreshWidgets();
			ClientQuestFile.INSTANCE.questTreeGui.chapterPanel.refreshWidgets();
			ClientQuestFile.INSTANCE.questTreeGui.viewQuestPanel.refreshWidgets();
		}
	}
}