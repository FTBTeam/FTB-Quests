package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageChangedTeam extends MessageToClient
{
	public UUID player;
	public short team;

	public MessageChangedTeam()
	{
	}

	public MessageChangedTeam(UUID id, short t)
	{
		player = id;
		team = t;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeUUID(player);
		data.writeShort(team);
	}

	@Override
	public void readData(DataIn data)
	{
		player = data.readUUID();
		team = data.readShort();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.exists())
		{
			if (team == 0)
			{
				ClientQuestFile.INSTANCE.playerTeams.removeShort(player);
			}
			else
			{
				ClientQuestFile.INSTANCE.playerTeams.put(player, team);
			}

			if (player.equals(Minecraft.getMinecraft().player.getUniqueID()))
			{
				ClientQuestFile.INSTANCE.self = ClientQuestFile.INSTANCE.getData(team);
			}
		}
	}
}