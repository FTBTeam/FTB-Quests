package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbguilibrary.icon.Icon;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

/**
 * @author LatvianModder
 */
public class MessageDisplayRewardToast extends MessageBase {
	private final long id;
	private final Component text;
	private final Icon icon;

	MessageDisplayRewardToast(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		text = buffer.readComponent();
		icon = NetUtils.readIcon(buffer);
	}

	public MessageDisplayRewardToast(long _id, Component t, Icon i) {
		id = _id;
		text = t;
		icon = i;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeComponent(text);
		NetUtils.writeIcon(buffer, icon);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.displayRewardToast(id, text, icon);
	}
}