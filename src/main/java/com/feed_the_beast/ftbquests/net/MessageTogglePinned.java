package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageTogglePinned extends MessageToServer
{
	private int id;

	public MessageTogglePinned()
	{
	}

	public MessageTogglePinned(int i)
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
	public void onMessage(EntityPlayerMP player)
	{
		int[] fav = NBTUtils.getPersistedData(player, false).getIntArray("ftbquests_pinned");

		IntOpenHashSet set = new IntOpenHashSet(fav.length);

		for (int i : fav)
		{
			set.add(i);
		}

		if (set.contains(id))
		{
			set.rem(id);
		}
		else
		{
			set.add(id);
		}

		NBTUtils.getPersistedData(player, true).setIntArray("ftbquests_pinned", set.toIntArray());
		new MessageTogglePinnedResponse(id).sendTo(player);
	}
}