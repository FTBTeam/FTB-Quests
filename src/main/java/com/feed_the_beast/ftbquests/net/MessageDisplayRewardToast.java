package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.util.NetUtils;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageDisplayRewardToast extends MessageBase
{
	private final int id;
	private final ITextComponent text;
	private final Icon icon;

	MessageDisplayRewardToast(PacketBuffer buffer)
	{
		id = buffer.readVarInt();
		text = buffer.readTextComponent();
		icon = NetUtils.readIcon(buffer);
	}

	public MessageDisplayRewardToast(int _id, ITextComponent t, Icon i)
	{
		id = _id;
		text = t;
		icon = i;
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		buffer.writeVarInt(id);
		buffer.writeTextComponent(text);
		NetUtils.writeIcon(buffer, icon);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		FTBQuests.NET_PROXY.displayRewardToast(id, text, icon);
	}
}