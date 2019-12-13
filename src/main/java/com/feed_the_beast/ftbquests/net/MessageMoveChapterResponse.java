package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuests;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageMoveChapterResponse extends MessageBase
{
	private final int id;
	private final boolean up;

	MessageMoveChapterResponse(PacketBuffer buffer)
	{
		id = buffer.readVarInt();
		up = buffer.readBoolean();
	}

	public MessageMoveChapterResponse(int i, boolean u)
	{
		id = i;
		up = u;
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		buffer.writeVarInt(id);
		buffer.writeBoolean(up);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		if (ClientQuestFile.INSTANCE != null)
		{
			Chapter chapter = ClientQuestFile.INSTANCE.getChapter(id);

			if (chapter != null)
			{
				int index = ClientQuestFile.INSTANCE.chapters.indexOf(chapter);

				if (index != -1 && up ? (index > 0) : (index < ClientQuestFile.INSTANCE.chapters.size() - 1))
				{
					ClientQuestFile.INSTANCE.chapters.remove(index);
					ClientQuestFile.INSTANCE.chapters.add(up ? index - 1 : index + 1, chapter);
					ClientQuestFile.INSTANCE.refreshIDMap();

					GuiQuests gui = ClientUtils.getCurrentGuiAs(GuiQuests.class);

					if (gui != null)
					{
						gui.chapterPanel.refreshWidgets();
						gui.chapterPanel.alignWidgets();
					}
				}
			}
		}
	}
}