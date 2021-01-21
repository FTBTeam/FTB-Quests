package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.util.NetUtils;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageDisplayRewardToast extends MessageBase
{
	private final int id;
	private final Component text;
	private final Icon icon;

	MessageDisplayRewardToast(FriendlyByteBuf buffer)
	{
		id = buffer.readVarInt();
		text = buffer.readComponent();
		icon = NetUtils.readIcon(buffer);
	}

	public MessageDisplayRewardToast(int _id, Component t, Icon i)
	{
		id = _id;
		text = t;
		icon = i;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeVarInt(id);
		buffer.writeComponent(text);
		NetUtils.writeIcon(buffer, icon);
	}

	@Override
	public void handle(NetworkManager.PacketContext context)
	{
		FTBQuests.NET_PROXY.displayRewardToast(id, text, icon);
	}
}