package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.IRewardListenerGui;
import com.feed_the_beast.ftbquests.gui.RewardKey;
import com.feed_the_beast.ftbquests.gui.RewardToast;
import com.feed_the_beast.ftbquests.util.NetUtils;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import net.minecraft.client.Minecraft;
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
		Icon i = icon.isEmpty() ? ClientQuestFile.INSTANCE.getBase(id).getIcon() : icon;

		if (!IRewardListenerGui.add(new RewardKey(text.getString(), i), 1))
		{
			Minecraft.getInstance().getToastGui().add(new RewardToast(text.getFormattedText(), i));
		}
	}
}